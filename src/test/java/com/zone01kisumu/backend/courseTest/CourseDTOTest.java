package com.zone01kisumu.backend.courseTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.CourseDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

// Test class for CourseDTO validation
class CourseDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCourseDTO() {
        CourseDTO dto = new CourseDTO(
                1L,
                "Java Programming",
                "Learn the basics of Java",
                "TECHNOLOGY",
                6,
                LocalDateTime.now(),
                LocalDateTime.now().plusWeeks(6),
                "LIVE",
                BigDecimal.valueOf(199.99),
                2L,
                "CREDIT_CARD",
                "TIL",
                "1234",
                "123-456-789",
                LocalDateTime.now(),
                "STARTED"
        );

        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "DTO should pass validation with no violations");
    }

    @Test
    void testInvalidCourseDTO_MissingRequiredFields() {
        CourseDTO dto = new CourseDTO(); // All fields null or blank

        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "DTO should have validation errors");

        violations.forEach(v -> System.out.println(v.getPropertyPath() + ": " + v.getMessage()));

        // Fields with @NotBlank or @NotNull should trigger validation errors
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")), "Title should be required");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("category")), "Category should be required");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duration")), "Duration should be required");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mode")), "Mode should be required");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")), "Price should be required");
    }

    @Test
    void testInvalidTitleTooLong() {
        String longTitle = "A".repeat(256); // exceeds max size (255)

        CourseDTO dto = new CourseDTO(
                1L,
                longTitle,
                "Too long title test",
                "OTHERS",
                10,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "RECORDED",
                BigDecimal.TEN,
                null,
                "CREDIT_CARD",
                "PAYBILL",
                "BuyGoodsPayment",
                "123",
                LocalDateTime.now(),
                "STARTED"
        );

        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should fail because title exceeds 255 characters");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")), "Title size should be violated");
    }

    @Test
    void testInvalidPaybillNumberTooLong() {
        String longPaybill = "9".repeat(101); // exceeds 100 chars

        CourseDTO dto = new CourseDTO(
                1L,
                "Short Course",
                "Desc",
                "TECHNOLOGY",
                4,
                LocalDateTime.now(),
                LocalDateTime.now().plusWeeks(4),
                "LIVE",
                BigDecimal.TEN,
                3L,
                "MPESA",
                "PAYBILL",
                longPaybill,
                "Account123",
                LocalDateTime.now(),
                "STARTED"
        );

        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should fail because paybillNumber exceeds 100 characters");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paybillNumber")), "Paybill number size should be violated");
    }

    @Test
    void testNegativePriceInvalid() {
        CourseDTO dto = new CourseDTO(
                1L,
                "Test Course",
                "Desc",
                "TECHNOLOGY",
                4,
                LocalDateTime.now(),
                LocalDateTime.now().plusWeeks(4),
                "LIVE",
                BigDecimal.valueOf(-5),
                3L,
                "MPESA",
                "PAYBILL",
                "12345",
                "Account123",
                LocalDateTime.now(),
                "STARTED"
        );

        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should fail because price is negative");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")), "Price must be non-negative");
    }
}
