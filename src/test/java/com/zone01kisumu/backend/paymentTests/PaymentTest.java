package com.zone01kisumu.backend.paymentTests;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        Long id = 1L;
        Long studentId = 101L;
        Long courseId = 202L;
        BigDecimal amount = new BigDecimal("2500.00");
        LocalDateTime paymentDate = LocalDateTime.now();
        Payment.PaymentMethod method = Payment.PaymentMethod.PAYPAL;
        Payment.PaymentStatus status = Payment.PaymentStatus.COMPLETED;
        String reference = "ref123";
        String accessCode = "acc123";

        Payment payment = new Payment(id, studentId, courseId, amount, paymentDate, method, status, reference, accessCode);

        assertEquals(id, payment.getId());
        assertEquals(studentId, payment.getStudentId());
        assertEquals(courseId, payment.getCourseId());
        assertEquals(amount, payment.getAmount());
        assertEquals(paymentDate, payment.getPaymentDate());
        assertEquals(method, payment.getPaymentMethod());
        assertEquals(status, payment.getStatus());
        assertEquals(reference, payment.getPaystackReference());
        assertEquals(accessCode, payment.getPaystackAccessCode());
    }

    @Test
    void testCustomConstructor() {
        Long studentId = 101L;
        Long courseId = 202L;
        BigDecimal amount = new BigDecimal("1000.00");
        Payment.PaymentMethod method = Payment.PaymentMethod.CREDIT_CARD;
        String reference = "customRef";

        Payment payment = new Payment(studentId, courseId, amount, method, reference);

        assertEquals(studentId, payment.getStudentId());
        assertEquals(courseId, payment.getCourseId());
        assertEquals(amount, payment.getAmount());
        assertEquals(method, payment.getPaymentMethod());
        assertEquals(reference, payment.getPaystackReference());
        // Default values
        assertEquals(Payment.PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void testDefaultValues() {
        Payment payment = new Payment();
        assertEquals(Payment.PaymentMethod.MPESA, payment.getPaymentMethod());
        assertEquals(Payment.PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void testSetters() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStudentId(100L);
        payment.setCourseId(200L);
        payment.setAmount(new BigDecimal("1500.00"));
        payment.setPaymentDate(LocalDateTime.of(2023, 5, 1, 12, 0));
        payment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setPaystackReference("xyz123");
        payment.setPaystackAccessCode("abc456");

        assertEquals(1L, payment.getId());
        assertEquals(100L, payment.getStudentId());
        assertEquals(200L, payment.getCourseId());
        assertEquals(new BigDecimal("1500.00"), payment.getAmount());
        assertEquals(LocalDateTime.of(2023, 5, 1, 12, 0), payment.getPaymentDate());
        assertEquals(Payment.PaymentMethod.BANK_TRANSFER, payment.getPaymentMethod());
        assertEquals(Payment.PaymentStatus.FAILED, payment.getStatus());
        assertEquals("xyz123", payment.getPaystackReference());
        assertEquals("abc456", payment.getPaystackAccessCode());
    }
}

