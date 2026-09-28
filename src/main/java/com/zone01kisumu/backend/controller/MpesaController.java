package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.config.MpesaConfigProperties;
import com.zone01kisumu.backend.dto.STKPushRequestDto;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.PaymentRepository;
import com.zone01kisumu.backend.service.SendSTKPush;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

@RestController
@RequestMapping("/api/mpesa")
@RequiredArgsConstructor
public class MpesaController {

    private final MpesaConfigProperties config;
    private final SendSTKPush sendSTKPush;

    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;
    private static final String RESULTCODE = "ResultCode";
    private static final String RESULTDESC = "ResultDesc";

    @PostMapping("/stk-push")
    public ResponseEntity<String> initiateSTKPush(@RequestBody STKPushRequestDto request) {
        try {
            String response = sendSTKPush.initiateSTKPush(
                    config.getPasskey(),
                    config.getCallbackUrl(),
                    config.getTimeoutUrl(),
                    request);

            return ResponseEntity.ok(response);

        } catch (JSONException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error initiating STK Push: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody String callbackData) {
        try {
            // Parse and process the callback data
            // You should implement proper validation and processing logic here

            // Return success response to M-Pesa
            JSONObject response = new JSONObject();
            response.put(RESULTCODE, "0");
            response.put(RESULTDESC, "Callback processed successfully");

            return ResponseEntity.ok(response.toString());

        } catch (JSONException e) {

            // Return error response to M-Pesa
            JSONObject errorResponse = new JSONObject();
            errorResponse.put(RESULTCODE, "1");
            errorResponse.put(RESULTDESC, "Error processing callback");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse.toString());
        }
    }

    @PostMapping("/timeout")
    public ResponseEntity<String> handleTimeout(@RequestBody String timeoutData) {
        try {
            // Handle timeout callback

            JSONObject response = new JSONObject();
            response.put(RESULTCODE, "0");
            response.put(RESULTDESC, "Timeout callback processed");

            return ResponseEntity.ok(response.toString());

        } catch (JSONException e) {

            JSONObject errorResponse = new JSONObject();
            errorResponse.put(RESULTCODE, "1");
            errorResponse.put(RESULTDESC, "Error processing timeout");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse.toString() + e.getMessage());
        }
    }

    @GetMapping("/transaction-status/{checkoutRequestId}")
    public ResponseEntity<String> checkTransactionStatus(@PathVariable String checkoutRequestId) {
        try {
            // 1. Find payment by checkoutRequestId
            Payment payment = paymentRepository.findByPaystackReference(checkoutRequestId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Payment not found for checkoutRequestId: " + checkoutRequestId));

            // 2. Get course from DB
            Course course = courseRepository.findById(payment.getCourseId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Course not found with ID: " + payment.getCourseId()));

            // 3. Use course.paymentAccount as BusinessShortCode
            String businessShortCode = course.getPaymentAccount();
            if (businessShortCode == null || businessShortCode.trim().isEmpty()) {
                throw new IllegalStateException("Course does not have a valid BusinessShortCode (paymentAccount)");
            }

            // 4. Generate timestamp and password
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = generatePassword(businessShortCode, config.getPasskey(), timestamp);

            // 5. Query status from Safaricom
            String response = sendSTKPush.sTKPushTransactionStatus(
                    businessShortCode,
                    password,
                    timestamp,
                    checkoutRequestId);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("IO error while checking transaction status: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug-config")
    public ResponseEntity<Map<String, String>> debugConfig() {
        Map<String, String> configs = new HashMap<>();
        configs.put("APP_KEY", this.config.getAppKey());
        configs.put("APP_SECRET", this.config.getAppSecret() != null ? "***SET***" : "NULL");
        configs.put("PASSKEY", this.config.getPasskey() != null ? "***SET***" : "NULL");
        configs.put("CALLBACK_URL", this.config.getCallbackUrl());
        configs.put("TIMEOUT_URL", this.config.getTimeoutUrl());
        configs.put("ENV", this.config.getEnv());
        return ResponseEntity.ok(configs);
    }

    private String generatePassword(String businessShortCode, String passkey, String timestamp) {
        String rawPassword = businessShortCode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

}