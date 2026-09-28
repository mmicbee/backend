package com.zone01kisumu.backend.paymentTests.paystackTests;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.exception.PaymentException;

import static org.junit.jupiter.api.Assertions.*;

class PaymentExceptionTest {

    @Test
    void testPaymentExceptionMessage() {
        String errorMessage = "Payment processing failed";

        PaymentException exception = new PaymentException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testPaymentExceptionIsRuntimeException() {
        PaymentException exception = new PaymentException("Something went wrong");

        assertTrue(exception instanceof RuntimeException);
    }
}

