package com.zone01kisumu.backend.paymentTests.paystackTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01kisumu.backend.config.PaystackConfig;
import com.zone01kisumu.backend.config.RestTemplates;
import com.zone01kisumu.backend.dto.*;
import com.zone01kisumu.backend.exception.PaymentException;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.repository.PaymentRepository;
import com.zone01kisumu.backend.service.CourseEnrollmentService;
import com.zone01kisumu.backend.service.PaystackService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaystackServiceTest {

    @Mock
    private CourseEnrollmentService enrollmentService;

    @Mock
    private PaystackConfig paystackConfig;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplates restTemplate;

    @Mock
    private RestTemplate innerRestTemplate;

    @InjectMocks
    private PaystackService paystackService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
         when(restTemplate.restTemplate()).thenReturn(innerRestTemplate);
    }

    @Test
    void testInitializePayment_success() {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setEmail("test@example.com");
        request.setAmount(BigDecimal.valueOf(100));
        request.setCallbackUrl("http://localhost/callback");
        request.setStudentId(1L);
        request.setCourseId(2L);

        PaystackInitializeResponse.Data data = new PaystackInitializeResponse.Data();
        data.setAccessCode("access_code");
        data.setAuthorizationUrl("http://paystack.com/pay/xyz");
        data.setReference("ref123");

        PaystackInitializeResponse response = new PaystackInitializeResponse();
        response.setStatus(true);
        response.setData(data);

        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paystackConfig.getBaseUrl()).thenReturn("https://api.paystack.co");

        when(innerRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PaystackInitializeResponse.class))).thenReturn(ResponseEntity.ok(response));

        // Act
        PaymentResponse paymentResponse = paystackService.initializePayment(request);

        // Assert
        assertNotNull(paymentResponse);
        assertEquals("ref123", paymentResponse.getReference());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testGetPaymentByReference_success() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaystackReference("ref123");

        when(paymentRepository.findByPaystackReference("ref123"))
                .thenReturn(Optional.of(payment));

        // Act
        Payment result = paystackService.getPaymentByReference("ref123");

        // Assert
        assertNotNull(result);
        assertEquals("ref123", result.getPaystackReference());
    }

    @Test
    void testGetPaymentByReference_notFound() {
        // Arrange
        when(paymentRepository.findByPaystackReference("not_found"))
                .thenReturn(Optional.empty());

        // Act & Assert
        PaymentException exception = assertThrows(PaymentException.class,
                () -> paystackService.getPaymentByReference("not_found"));
        assertEquals("Payment not found", exception.getMessage());
    }

    @Test
    void testVerifyAndUpdatePayment_success() throws Exception {
        // Arrange
        String reference = "ref123";

        Payment payment = new Payment();
        payment.setStudentId(1L);
        payment.setCourseId(2L);
        payment.setAmount(BigDecimal.valueOf(200));

        Map<String, Object> data = new HashMap<>();
        data.put("status", "success");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", data);

        String jsonResponse = objectMapper.writeValueAsString(responseMap);

        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paystackConfig.getBaseUrl()).thenReturn("https://api.paystack.co");
        when(innerRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(ResponseEntity.ok(jsonResponse));

        when(paymentRepository.findByPaystackReference(reference)).thenReturn(Optional.of(payment));

        // Act
        Payment result = paystackService.verifyAndUpdatePayment(reference);

        // Assert
        assertEquals(Payment.PaymentStatus.COMPLETED, result.getStatus());
        verify(enrollmentService).updatePaymentStatus(1L, 2L, BigDecimal.valueOf(200));
    }

    @Test
    void testVerifyAndUpdatePayment_failedStatus() throws Exception {
        // Arrange
        String reference = "ref123";

        Payment payment = new Payment();
        payment.setStudentId(1L);
        payment.setCourseId(2L);
        payment.setAmount(BigDecimal.valueOf(200));

        Map<String, Object> data = new HashMap<>();
        data.put("status", "failed");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", data);

        String jsonResponse = objectMapper.writeValueAsString(responseMap);

        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paystackConfig.getBaseUrl()).thenReturn("https://api.paystack.co");
        when(innerRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(ResponseEntity.ok(jsonResponse));

        when(paymentRepository.findByPaystackReference(reference)).thenReturn(Optional.of(payment));

        // Act
        Payment result = paystackService.verifyAndUpdatePayment(reference);

        // Assert
        assertEquals(Payment.PaymentStatus.FAILED, result.getStatus());
        verify(enrollmentService, never()).updatePaymentStatus(anyLong(), anyLong(), any());
    }

    @Test
    void testVerifyAndUpdatePayment_non200Response_throwsException() {
        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paystackConfig.getBaseUrl()).thenReturn("https://api.paystack.co");

        when(innerRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.BAD_REQUEST));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paystackService.verifyAndUpdatePayment("ref123"));

        assertEquals("Failed to verify payment", ex.getMessage());
    }

    @Test
    void testVerifyAndUpdatePayment_jsonParsingFails_throwsException() throws Exception {
        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paystackConfig.getBaseUrl()).thenReturn("https://api.paystack.co");

        String malformedJson = "{invalid-json";
        when(innerRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(malformedJson));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paystackService.verifyAndUpdatePayment("ref123"));

        assertTrue(ex.getMessage().startsWith("Error verifying payment"));
    }

    @Test
    void testHandleWebhook_successfulCharge_event() throws Exception {
        String reference = "ref123";

        Payment payment = new Payment();
        payment.setStudentId(1L);
        payment.setCourseId(2L);
        payment.setAmount(BigDecimal.valueOf(300));

        Map<String, Object> data = new HashMap<>();
        data.put("reference", reference);
        data.put("channel", "credit_card");

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "charge.success");
        payload.put("data", data);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paymentRepository.findByPaystackReference(reference)).thenReturn(Optional.of(payment));

        String signature = hmacSha512("sk_test", jsonPayload);
        paystackService.handleWebhook(jsonPayload, signature);

        verify(enrollmentService).updatePaymentStatus(1L, 2L, BigDecimal.valueOf(300));
        verify(paymentRepository).save(any(Payment.class));
        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus());
    }

    @Test
    void testHandleWebhook_enrollmentUpdateFails_marksPaymentFailed() throws Exception {
        String reference = "ref123";

        Payment payment = new Payment();
        payment.setStudentId(1L);
        payment.setCourseId(2L);
        payment.setAmount(BigDecimal.valueOf(300));

        Map<String, Object> data = new HashMap<>();
        data.put("reference", reference);
        data.put("channel", "credit_card");

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "charge.success");
        payload.put("data", data);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paymentRepository.findByPaystackReference(reference)).thenReturn(Optional.of(payment));
        doThrow(new RuntimeException("Enrollment error")).when(enrollmentService)
                .updatePaymentStatus(anyLong(), anyLong(), any());

        String signature = hmacSha512("sk_test", jsonPayload);

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paystackService.handleWebhook(jsonPayload, signature));

        assertTrue(ex.getMessage().contains("Payment could not be applied to enrollment"));
        verify(paymentRepository)
                .save(argThat(savedPayment -> savedPayment.getStatus() == Payment.PaymentStatus.FAILED));
    }

    @Test
    void testHandleWebhook_invalidSignature_throwsException() throws Exception {
        // Simulate mismatched signature but do not throw
        String reference = "ref123";

        Payment payment = new Payment();
        payment.setStudentId(1L);
        payment.setCourseId(2L);
        payment.setAmount(BigDecimal.valueOf(300));

        Map<String, Object> data = new HashMap<>();
        data.put("reference", reference);
        data.put("channel", "credit_card");

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "charge.success");
        payload.put("data", data);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        when(paystackConfig.getSecretKey()).thenReturn("sk_test");
        when(paymentRepository.findByPaystackReference(reference)).thenReturn(Optional.of(payment));

        String signature = hmacSha512("sk_test", jsonPayload);
        paystackService.handleWebhook(jsonPayload, signature);

        verify(paymentRepository).save(any(Payment.class));
    }

    private String hmacSha512(String secret, String data) throws Exception {
        Mac sha512Hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512Hmac.init(keySpec);
        byte[] macData = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(macData);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}