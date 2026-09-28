package com.zone01kisumu.backend.paymentTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.PaymentInitiationRequest;

import jakarta.validation.*;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentInitiationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidPaymentInitiationRequest() {
        PaymentInitiationRequest request = new PaymentInitiationRequest(
                1L,
                100L,
                200L,
                new BigDecimal("2500.00"),
                "MPESA",
                "student@example.com",
                "https://callback.url"
        );

        Set<ConstraintViolation<PaymentInitiationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected no validation errors");
    }

    @Test
    void testNullFieldsTriggerValidationErrors() {
        PaymentInitiationRequest request = new PaymentInitiationRequest();

        Set<ConstraintViolation<PaymentInitiationRequest>> violations = validator.validate(request);
        assertEquals(5, violations.size()); // studentId, courseId, amount, email, paymentMethod

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("studentId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("courseId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentMethod")));
    }

    @Test
    void testAmountMustBeGreaterThanZero() {
        PaymentInitiationRequest request = new PaymentInitiationRequest(
                1L,
                100L,
                200L,
                new BigDecimal("0.00"),  // Invalid
                "MPESA",
                "student@example.com",
                null
        );

        Set<ConstraintViolation<PaymentInitiationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                v -> v.getPropertyPath().toString().equals("amount") &&
                     v.getMessage().contains("greater than 0")
        ));
    }

    @Test
    void testBlankPaymentMethod() {
        PaymentInitiationRequest request = new PaymentInitiationRequest(
                1L,
                100L,
                200L,
                new BigDecimal("500.00"),
                "   ", // Invalid blank
                "student@example.com",
                null
        );

        Set<ConstraintViolation<PaymentInitiationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                v -> v.getPropertyPath().toString().equals("paymentMethod") &&
                     v.getMessage().contains("required")
        ));
    }

    @Test
    void testMissingEmail() {
        PaymentInitiationRequest request = new PaymentInitiationRequest(
                1L,
                100L,
                200L,
                new BigDecimal("500.00"),
                "MPESA",
                null, // Email missing
                null
        );

        Set<ConstraintViolation<PaymentInitiationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                v -> v.getPropertyPath().toString().equals("email") &&
                     v.getMessage().contains("required")
        ));
    }
}
