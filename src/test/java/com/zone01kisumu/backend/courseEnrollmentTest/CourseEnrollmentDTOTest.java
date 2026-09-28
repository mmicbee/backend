package com.zone01kisumu.backend.courseEnrollmentTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.CourseEnrollmentDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

// This test class is used to validate the CourseEnrollmentDTO
class CourseEnrollmentDTOTest {

    private Validator validator;

    @BeforeEach
    void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidDTO() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO(
                1L,
                100L,
                200L,
                LocalDateTime.now(),
                "COMPLETED",
                "MPESA",
                new BigDecimal("150.00"),
                new BigDecimal("0.00")
        );

        Set<ConstraintViolation<CourseEnrollmentDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMissingRequiredFields() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();

        Set<ConstraintViolation<CourseEnrollmentDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("studentId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("courseId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("enrollmentDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentStatus")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentMethod")));
    }

    @Test
    void testBlankPaymentFields() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO(
                1L,
                101L,
                201L,
                LocalDateTime.now(),
                "   ",  // Invalid (blank)
                "",     // Invalid (blank)
                new BigDecimal("100"),
                new BigDecimal("0")
        );

        Set<ConstraintViolation<CourseEnrollmentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentStatus")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentMethod")));
    }

    @Test
    void testNegativeAmounts() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO(
                2L,
                102L,
                202L,
                LocalDateTime.now(),
                "PENDING",
                "BANK_TRANSFER",
                new BigDecimal("-10.00"),
                new BigDecimal("-5.00")
        );

        Set<ConstraintViolation<CourseEnrollmentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amountPaidNow")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amountRemaining")));
    }

    @Test
    void testZeroAmountsValid() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO(
                3L,
                103L,
                203L,
                LocalDateTime.now(),
                "COMPLETED",
                "PAYPAL",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        Set<ConstraintViolation<CourseEnrollmentDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Zero values should be allowed for amounts");
    }

    @Test
    void testGettersAndSetters() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        LocalDateTime now = LocalDateTime.now();

        dto.setId(10L);
        dto.setStudentId(123L);
        dto.setCourseId(456L);
        dto.setEnrollmentDate(now);
        dto.setPaymentStatus("COMPLETED");
        dto.setPaymentMethod("CREDIT_CARD");
        dto.setAmountPaidNow(new BigDecimal("500.00"));
        dto.setAmountRemaining(new BigDecimal("0.00"));

        assertEquals(10L, dto.getId());
        assertEquals(123L, dto.getStudentId());
        assertEquals(456L, dto.getCourseId());
        assertEquals(now, dto.getEnrollmentDate());
        assertEquals("COMPLETED", dto.getPaymentStatus());
        assertEquals("CREDIT_CARD", dto.getPaymentMethod());
        assertEquals(new BigDecimal("500.00"), dto.getAmountPaidNow());
        assertEquals(new BigDecimal("0.00"), dto.getAmountRemaining());
    }
}
