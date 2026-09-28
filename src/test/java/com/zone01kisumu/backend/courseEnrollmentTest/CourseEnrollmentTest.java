package com.zone01kisumu.backend.courseEnrollmentTest;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseEnrollment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

// This test class is used to validate the CourseEnrollment entity
class CourseEnrollmentTest {

    @Test
    void testBuilderAndGetters() {
        Course course = new Course(); // You may need to create a mock or minimal Course class
        course.setId(1L);

        BigDecimal due = new BigDecimal("100.00");
        BigDecimal paid = new BigDecimal("50.00");

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .studentId(10L)
                .course(course)
                .amountDue(due)
                .amountPaid(paid)
                .paymentMethod(CourseEnrollment.PaymentMethod.MPESA)
                .paymentStatus(CourseEnrollment.PaymentStatus.PARTIALLY_PAID)
                .build();

        assertNotNull(enrollment);
        assertEquals(10L, enrollment.getStudentId());
        assertEquals(course, enrollment.getCourse());
        assertEquals(due, enrollment.getAmountDue());
        assertEquals(paid, enrollment.getAmountPaid());
        assertEquals(CourseEnrollment.PaymentMethod.MPESA, enrollment.getPaymentMethod());
        assertEquals(CourseEnrollment.PaymentStatus.PARTIALLY_PAID, enrollment.getPaymentStatus());
    }

    @Test
    void testDefaultValues() {
        Course course = new Course();
        course.setId(2L);

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .studentId(20L)
                .course(course)
                .build();

        assertEquals(CourseEnrollment.PaymentMethod.CREDIT_CARD, enrollment.getPaymentMethod());
        assertEquals(CourseEnrollment.PaymentStatus.PENDING, enrollment.getPaymentStatus());
    }

    @Test
    void testConstructorWithIdOnly() {
        CourseEnrollment enrollment = new CourseEnrollment(99L);
        assertEquals(99L, enrollment.getId());
    }

    @Test
    void testSettersAndGetters() {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(1L);
        enrollment.setStudentId(5L);
        enrollment.setAmountDue(new BigDecimal("300.00"));
        enrollment.setAmountPaid(new BigDecimal("100.00"));
        enrollment.setPaymentMethod(CourseEnrollment.PaymentMethod.BANK_TRANSFER);
        enrollment.setPaymentStatus(CourseEnrollment.PaymentStatus.FAILED);

        assertEquals(1L, enrollment.getId());
        assertEquals(5L, enrollment.getStudentId());
        assertEquals(new BigDecimal("300.00"), enrollment.getAmountDue());
        assertEquals(new BigDecimal("100.00"), enrollment.getAmountPaid());
        assertEquals(CourseEnrollment.PaymentMethod.BANK_TRANSFER, enrollment.getPaymentMethod());
        assertEquals(CourseEnrollment.PaymentStatus.FAILED, enrollment.getPaymentStatus());
    }

    @Test
    void testEnrollmentDateIsNullBeforePersist() {
        CourseEnrollment enrollment = CourseEnrollment.builder()
                .studentId(33L)
                .course(new Course())
                .build();

        assertNull(enrollment.getEnrollmentDate(), "Enrollment date should be null before persisting");
    }
}

