package com.zone01kisumu.backend.paymentTests.paystackTests;

import com.zone01kisumu.backend.controller.PaymentController;
import com.zone01kisumu.backend.dto.PaymentInitiationRequest;
import com.zone01kisumu.backend.dto.PaymentResponse;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.service.PaystackService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock
    private PaystackService paystackService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void initializePayment_success() {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setAmount(BigDecimal.valueOf(1000));
        request.setEmail("test@example.com");
        request.setPaymentMethod("card");
        request.setCallbackUrl("http://callback.url");

        PaymentResponse response = new PaymentResponse("http://auth.url", "ref123", "access123");

        when(paystackService.initializePayment(request)).thenReturn(response);

        // Act
        ResponseEntity<Object> result = paymentController.initializePayment(request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void initializePayment_error() {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setAmount(BigDecimal.valueOf(1000));
        request.setEmail("test@example.com");
        request.setPaymentMethod("card");
        request.setCallbackUrl("http://callback.url");

        when(paystackService.initializePayment(request))
                .thenThrow(new RuntimeException("Failed to init"));

        // Act
        ResponseEntity<Object> result = paymentController.initializePayment(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("Failed to init"));
    }

    @Test
    void verifyPayment_success() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaystackReference("ref123");

        when(paystackService.verifyAndUpdatePayment("ref123")).thenReturn(payment);

        // Act
        ResponseEntity<Object> result = paymentController.verifyPayment("ref123");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(payment, result.getBody());
    }

    @Test
    void verifyPayment_error() {
        // Arrange
        when(paystackService.verifyAndUpdatePayment("ref123"))
                .thenThrow(new RuntimeException("Verification failed"));

        // Act
        ResponseEntity<Object> result = paymentController.verifyPayment("ref123");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("Verification failed"));
    }

    @Test
    void handleWebhook_success() {
        // Arrange
        String payload = "{\"event\":\"charge.success\"}";
        String signature = "fake-signature";

        doNothing().when(paystackService).handleWebhook(payload, signature);

        // Act
        ResponseEntity<String> result = paymentController.handleWebhook(payload, signature);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Webhook processed successfully", result.getBody());
    }

    @Test
    void handleWebhook_failure() {
        // Arrange
        String payload = "{\"event\":\"charge.success\"}";
        String signature = "fake-signature";

        doThrow(new RuntimeException("Signature mismatch"))
                .when(paystackService).handleWebhook(payload, signature);

        // Act
        ResponseEntity<String> result = paymentController.handleWebhook(payload, signature);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().contains("Webhook processing failed"));
    }

    @Test
    void getPayment_found() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaystackReference("ref123");

        when(paystackService.getPaymentByReference("ref123")).thenReturn(payment);

        // Act
        ResponseEntity<Payment> result = paymentController.getPayment("ref123");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(payment, result.getBody());
    }

    @Test
    void getPayment_notFound() {
        // Arrange
        when(paystackService.getPaymentByReference("ref404"))
                .thenThrow(new RuntimeException("Not found"));

        // Act
        ResponseEntity<Payment> result = paymentController.getPayment("ref404");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }
}