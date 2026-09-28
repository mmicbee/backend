package com.zone01kisumu.backend.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01kisumu.backend.config.PaystackConfig;
import com.zone01kisumu.backend.config.RestTemplates;
import com.zone01kisumu.backend.dto.*;
import com.zone01kisumu.backend.exception.PaymentException;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaystackService {

    private final CourseEnrollmentService enrollmentService;
    private final PaystackConfig paystackConfig;
    private final PaymentRepository paymentRepository;
    private static final String PAYMENTNOTFOUND = "Payment not found";

    private final RestTemplates restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentResponse initializePayment(PaymentInitiationRequest request) {
        try {
            // Generate unique reference(first step)
            String reference = generateReference();

            // Prepare Paystack initialization request(2nd step)
            Map<String, Object> paystackRequest = new HashMap<>();
            paystackRequest.put("email", request.getEmail());
            paystackRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Convert
                                                                                                             // to kobo
            paystackRequest.put("reference", reference);
            paystackRequest.put("callback_url", request.getCallbackUrl());
            List<String> channels = new ArrayList<>();
            channels.add("card");
            channels.add("bank_transfer");
            paystackRequest.put("channels", channels);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(paystackConfig.getSecretKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paystackRequest, headers);

            // Make request to Paystack(3rd step)
            ResponseEntity<PaystackInitializeResponse> response = restTemplate.restTemplate().exchange(
                    paystackConfig.getBaseUrl() + "/transaction/initialize",
                    HttpMethod.POST,
                    entity,
                    PaystackInitializeResponse.class);

            // Step 4: handle response
            if (response.getBody() != null && response.getBody().isStatus()) {
                PaystackInitializeResponse.Data data = response.getBody().getData();

                // Create payment record in database
                // Update payment record with access code
                Payment payment = new Payment(
                        request.getStudentId(),
                        request.getCourseId(),
                        request.getAmount(),
                        Payment.PaymentMethod.CREDIT_CARD, // Default to credit card for Paystack
                        reference);
                payment.setPaystackAccessCode(data.getAccessCode());
                payment.setPaymentDate(java.time.LocalDateTime.now());
                paymentRepository.save(payment);

                return new PaymentResponse(
                        data.getAuthorizationUrl(),
                        data.getReference(),
                        data.getAccessCode());
            } else {
                throw new PaymentException("Failed to initialize payment: " + response.getBody().getMessage());
            }

        } catch (Exception e) {
            throw new PaymentException("Error initializing payment: " + e.getMessage());
        }
    }

    public Payment verifyAndUpdatePayment(String reference) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(paystackConfig.getSecretKey());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.restTemplate().exchange(
                    paystackConfig.getBaseUrl() + "/transaction/verify/" + reference,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                Payment payment = paymentRepository.findByPaystackReference(reference)
                        .orElseThrow(() -> new PaymentException("Payment not found"));

                String status = (String) data.get("status");

                if ("success".equalsIgnoreCase(status)) {
                    payment.setStatus(Payment.PaymentStatus.COMPLETED);
                    // Update enrollment payment status
                    enrollmentService.updatePaymentStatus(
                            payment.getStudentId(),
                            payment.getCourseId(),
                            payment.getAmount());
                } else if ("failed".equalsIgnoreCase(status)) {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                }
                payment.setPaymentDate(java.time.LocalDateTime.now());
                paymentRepository.save(payment);
                return payment;
            }

            throw new PaymentException("Failed to verify payment");
        } catch (IOException e) {
            throw new PaymentException("Error verifying payment: " + e.getMessage());
        }
    }

    public void handleWebhook(String payload, String signature) {
        try {
            // Verify signature
            String computedHash = hmacSha512(paystackConfig.getSecretKey(), payload);
            if (!computedHash.equals(signature)) {
                // Comment out for dev test print a mesaage to console
                throw new PaymentException("Invalid Paystack signature");
            }

            // Parse JSON payload
            Map<String, Object> body = objectMapper.readValue(payload, Map.class);
            String event = (String) body.get("event");

            if ("charge.success".equals(event)) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String reference = (String) data.get("reference");

                Payment payment = paymentRepository.findByPaystackReference(reference)
                        .orElseThrow(() -> new PaymentException("Payment not found"));

                String channel = (String) data.get("channel"); // card, bank_transfer, etc.

                try {
                    // Update enrollment with amount
                    enrollmentService.updatePaymentStatus(
                            payment.getStudentId(),
                            payment.getCourseId(),
                            payment.getAmount());

                    // If successful, mark payment as completed
                    payment.setStatus(Payment.PaymentStatus.COMPLETED);
                    payment.setPaymentMethod(Payment.PaymentMethod.valueOf(channel.toUpperCase()));
                    payment.setPaymentDate(java.time.LocalDateTime.now());
                    paymentRepository.save(payment);
                } catch (Exception e) {
                    // if not mark payment as FAILED
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    payment.setPaymentMethod(Payment.PaymentMethod.valueOf(channel.toUpperCase()));
                    payment.setPaymentDate(java.time.LocalDateTime.now());
                    paymentRepository.save(payment);
                    throw new PaymentException("Payment could not be applied to enrollment: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new PaymentException("Error processing webhook: " + e.getMessage());
        }
    }

    private String hmacSha512(String key, String data) throws Exception {
        Mac sha512Hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512Hmac.init(keySpec);
        byte[] macData = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : macData) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public Payment getPaymentByReference(String reference) {
        return paymentRepository.findByPaystackReference(reference)
                .orElseThrow(() -> new PaymentException(PAYMENTNOTFOUND));
    }

    private String generateReference() {
        return "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
