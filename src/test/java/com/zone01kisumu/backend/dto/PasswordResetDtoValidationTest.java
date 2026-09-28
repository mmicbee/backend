package com.zone01kisumu.backend.dto;

import com.zone01kisumu.backend.dto.PasswordResetDTOs.ForgotPasswordRequest;
import com.zone01kisumu.backend.dto.PasswordResetDTOs.ResetPasswordRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ================ ForgotPasswordRequest Validation Tests ================

    @Test
    void forgotPasswordRequest_ShouldBeValid_WhenAllFieldsProvided() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("student")
                .build();

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid ForgotPasswordRequest should have no violations");
    }

    @Test
    void forgotPasswordRequest_ShouldBeInvalid_WhenPhoneOrEmailIsNull() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail(null)
                .role("student")
                .build();

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void forgotPasswordRequest_ShouldBeInvalid_WhenPhoneOrEmailIsBlank() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("   ")
                .role("student")
                .build();

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void forgotPasswordRequest_ShouldBeInvalid_WhenRoleIsNull() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("user@example.com")
                .role(null)
                .build();

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void forgotPasswordRequest_ShouldBeInvalid_WhenRoleIsBlank() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("  ")
                .build();

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    // ================ ResetPasswordRequest Validation Tests ================

    @Test
    void resetPasswordRequest_ShouldBeValid_WhenAllFieldsProvided() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("123456")
                .newPassword("strongPassword123")
                .role("student")
                .build();

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid ResetPasswordRequest should have no violations");
    }

    @Test
    void resetPasswordRequest_ShouldBeInvalid_WhenOtpCodeIsBlank() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("")
                .newPassword("strongPassword123")
                .role("student")
                .build();

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void resetPasswordRequest_ShouldBeInvalid_WhenNewPasswordIsBlank() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("123456")
                .newPassword("")
                .role("student")
                .build();

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void resetPasswordRequest_ShouldBeInvalid_WhenAllFieldsMissing() {
        ResetPasswordRequest request = new ResetPasswordRequest();

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(4, violations.size(), "Should have violations for all 4 missing fields");
    }
}
