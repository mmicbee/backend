package com.zone01kisumu.backend.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OtpDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ================ OtpSendRequest Validation Tests ================

    @Test
    void otpSendRequest_ShouldBeValid_WhenAllRequiredFieldsProvided() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("student")
                .otpMethod("email")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid OtpSendRequest should have no violations");
    }

    @Test
    void otpSendRequest_ShouldBeValid_WhenOnlyRequiredFieldsProvided() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("student")
                .build(); // otpMethod is optional

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "OtpSendRequest with only required fields should be valid");
    }

    @Test
    void otpSendRequest_ShouldBeInvalid_WhenPhoneOrEmailIsNull() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail(null)
                .role("student")
                .otpMethod("email")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
    }

    @Test
    void otpSendRequest_ShouldBeInvalid_WhenPhoneOrEmailIsEmpty() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("")
                .role("student")
                .otpMethod("email")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
    }

    @Test
    void otpSendRequest_ShouldBeInvalid_WhenPhoneOrEmailIsBlank() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("   ")
                .role("student")
                .otpMethod("email")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
    }

    @Test
    void otpSendRequest_ShouldBeInvalid_WhenRoleIsNull() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role(null)
                .otpMethod("email")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    void otpSendRequest_ShouldBeInvalid_WhenRoleIsEmpty() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("")
                .otpMethod("email")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    void otpSendRequest_ShouldBeInvalid_WhenRoleIsBlank() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("   ")
                .otpMethod("email")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    void otpSendRequest_ShouldBeValid_WhenOtpMethodIsNull() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("student")
                .otpMethod(null)
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "OtpMethod is optional, so null should be valid");
    }

    @Test
    void otpSendRequest_ShouldBeValid_WhenOtpMethodIsEmpty() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("student")
                .otpMethod("")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "OtpMethod is optional, so empty should be valid");
    }

    @Test
    void otpSendRequest_ShouldBeInvalid_WhenMultipleFieldsMissing() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("")
                .role("")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size(), "Should have violations for both missing fields");

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    // ================ OtpVerifyRequest Validation Tests ================

    @Test
    void otpVerifyRequest_ShouldBeValid_WhenAllRequiredFieldsProvided() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("123456")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid OtpVerifyRequest should have no violations");
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenPhoneOrEmailIsNull() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail(null)
                .otpCode("123456")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenPhoneOrEmailIsEmpty() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("")
                .otpCode("123456")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenPhoneOrEmailIsBlank() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("   ")
                .otpCode("123456")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenOtpCodeIsNull() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode(null)
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("OTP code is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenOtpCodeIsEmpty() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("OTP code is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenOtpCodeIsBlank() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("   ")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("OTP code is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenRoleIsNull() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("123456")
                .role(null)
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenRoleIsEmpty() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("123456")
                .role("")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenRoleIsBlank() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("123456")
                .role("   ")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    void otpVerifyRequest_ShouldBeInvalid_WhenAllFieldsMissing() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("")
                .otpCode("")
                .role("")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);

        assertEquals(3, violations.size(), "Should have violations for all missing fields");

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone or email is required")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("OTP code is required")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    // ================ Field Content Validation Tests ================

    @Test
    void otpSendRequest_ShouldBeValid_WithValidEmailFormats() {
        String[] validEmails = {
            "user@example.com",
            "test.email@domain.co.uk",
            "user+tag@example.org",
            "123@456.com",
            "a@b.co"
        };

        for (String email : validEmails) {
            OtpSendRequest request = OtpSendRequest.builder()
                    .phoneOrEmail(email)
                    .role("student")
                    .build();

            Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Email '" + email + "' should be valid");
        }
    }

    @Test
    void otpSendRequest_ShouldBeValid_WithValidPhoneNumbers() {
        String[] validPhones = {
            "+1234567890",
            "1234567890",
            "+44 20 7946 0958",
            "(555) 123-4567"
        };

        for (String phone : validPhones) {
            OtpSendRequest request = OtpSendRequest.builder()
                    .phoneOrEmail(phone)
                    .role("student")
                    .build();

            Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Phone '" + phone + "' should be valid");
        }
    }

    @Test
    void otpSendRequest_ShouldBeValid_WithValidRoles() {
        String[] validRoles = {"student", "teacher", "institution", "STUDENT", "TEACHER", "INSTITUTION"};

        for (String role : validRoles) {
            OtpSendRequest request = OtpSendRequest.builder()
                    .phoneOrEmail("user@example.com")
                    .role(role)
                    .build();

            Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Role '" + role + "' should be valid");
        }
    }

    @Test
    void otpSendRequest_ShouldBeValid_WithValidOtpMethods() {
        String[] validMethods = {"email", "sms", "EMAIL", "SMS", null, ""};

        for (String method : validMethods) {
            OtpSendRequest request = OtpSendRequest.builder()
                    .phoneOrEmail("user@example.com")
                    .role("student")
                    .otpMethod(method)
                    .build();

            Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "OtpMethod '" + method + "' should be valid");
        }
    }

    @Test
    void otpVerifyRequest_ShouldBeValid_WithValidOtpCodes() {
        String[] validOtpCodes = {
            "123456",
            "000000",
            "999999",
            "ABC123", // Some systems might use alphanumeric
            "1a2b3c"
        };

        for (String otpCode : validOtpCodes) {
            OtpVerifyRequest request = OtpVerifyRequest.builder()
                    .phoneOrEmail("user@example.com")
                    .otpCode(otpCode)
                    .role("student")
                    .build();

            Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "OtpCode '" + otpCode + "' should be valid");
        }
    }

    // ================ Edge Cases and Special Characters ================

    @Test
    void otpSendRequest_ShouldBeValid_WithSpecialCharactersInEmail() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user+test@example-domain.com")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void otpSendRequest_ShouldBeValid_WithSpecialCharactersInPhone() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("+1 (555) 123-4567")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void otpVerifyRequest_ShouldBeValid_WithSpecialCharactersInOtpCode() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("A1-B2")
                .role("student")
                .build();

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    // ================ Builder Pattern Tests ================

    @Test
    void otpSendRequest_ShouldSupportBuilderPattern() {
        OtpSendRequest request = OtpSendRequest.builder()
                .phoneOrEmail("user@example.com")
                .role("student")
                .otpMethod("email")
                .build();

        assertNotNull(request);
        assertEquals("user@example.com", request.getPhoneOrEmail());
        assertEquals("student", request.getRole());
        assertEquals("email", request.getOtpMethod());
    }

    @Test
    void otpVerifyRequest_ShouldSupportBuilderPattern() {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .phoneOrEmail("user@example.com")
                .otpCode("123456")
                .role("student")
                .build();

        assertNotNull(request);
        assertEquals("user@example.com", request.getPhoneOrEmail());
        assertEquals("123456", request.getOtpCode());
        assertEquals("student", request.getRole());
    }

    @Test
    void otpSendRequest_ShouldSupportNoArgsConstructor() {
        OtpSendRequest request = new OtpSendRequest();
        assertNotNull(request);

        // Should be invalid due to missing required fields
        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void otpVerifyRequest_ShouldSupportNoArgsConstructor() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        assertNotNull(request);

        // Should be invalid due to missing required fields
        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void otpSendRequest_ShouldSupportAllArgsConstructor() {
        OtpSendRequest request = new OtpSendRequest("user@example.com", "student", "email");

        assertNotNull(request);
        assertEquals("user@example.com", request.getPhoneOrEmail());
        assertEquals("student", request.getRole());
        assertEquals("email", request.getOtpMethod());

        Set<ConstraintViolation<OtpSendRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void otpVerifyRequest_ShouldSupportAllArgsConstructor() {
        OtpVerifyRequest request = new OtpVerifyRequest("user@example.com", "123456", "student");

        assertNotNull(request);
        assertEquals("user@example.com", request.getPhoneOrEmail());
        assertEquals("123456", request.getOtpCode());
        assertEquals("student", request.getRole());

        Set<ConstraintViolation<OtpVerifyRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}