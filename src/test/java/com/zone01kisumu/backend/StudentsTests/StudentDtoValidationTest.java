package com.zone01kisumu.backend.StudentsTests;

import com.zone01kisumu.backend.dto.StudentLogin;
import com.zone01kisumu.backend.dto.StudentRegistration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StudentDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // --- StudentRegistration Tests ---

    @Test
    @DisplayName("Should fail when all fields are blank in StudentRegistration")
    void StudentRegistration_AllBlankFields() {
        StudentRegistration dto = StudentRegistration.builder().build();
        Set<ConstraintViolation<StudentRegistration>> violations = validator.validate(dto);

        assertThat(violations).hasSize(5); // All fields missing
    }

    @Test
    @DisplayName("Should fail when email is invalid in StudentRegistration")
    void StudentRegistration_InvalidEmail() {
        StudentRegistration dto = StudentRegistration.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .phone("0712345678")
                .profilePicture(".profile.png")
                .password("securepass")
                .build();

        Set<ConstraintViolation<StudentRegistration>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Should fail when password is too short in StudentRegistration")
    void StudentRegistration_ShortPassword() {
        StudentRegistration dto = StudentRegistration.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("0712345678")
                .profilePicture(".profile.png")
                .password("123") // too short
                .build();

        Set<ConstraintViolation<StudentRegistration>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Should pass with valid StudentRegistration")
    void StudentRegistration_ValidInput() {
        StudentRegistration dto = StudentRegistration.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("0712345678")
                .profilePicture(".profile.png")
                .password("securePassword123")
                .build();

        Set<ConstraintViolation<StudentRegistration>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    // --- StudentLogin Tests ---

    @Test
    @DisplayName("Should fail when fields are blank in StudentLogin")
    void StudentLogin_BlankFields() {
        StudentLogin dto = StudentLogin.builder().build();

        Set<ConstraintViolation<StudentLogin>> violations = validator.validate(dto);
        assertThat(violations).hasSize(2); // email and password missing
    }

    @Test
    @DisplayName("Should fail with invalid email format in StudentLogin")
    void StudentLogin_InvalidEmail() {
        StudentLogin dto = StudentLogin.builder()
                .email("invalid-email")
                .password("securePass")
                .build();

        Set<ConstraintViolation<StudentLogin>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Should pass with valid StudentLogin")
    void StudentLogin_ValidInput() {
        StudentLogin dto = StudentLogin.builder()
                .email("login@example.com")
                .password("strongPassword")
                .build();

        Set<ConstraintViolation<StudentLogin>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}