package com.zone01kisumu.backend.TeacherTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.TeacherLogin;
import com.zone01kisumu.backend.dto.TeacherRegistration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class TeacherDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // --- TeacherRegistration Tests ---

    @Test
    @DisplayName("Should fail when all required fields are blank")
    void teacherRegistration_AllBlankFields() {
        TeacherRegistration dto = TeacherRegistration.builder().build();
        Set<ConstraintViolation<TeacherRegistration>> violations = validator.validate(dto);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(5); 
    }

    @Test
    @DisplayName("Should fail when email is invalid in TeacherRegistration")
    void teacherRegistration_InvalidEmail() {
        TeacherRegistration dto = validRegistration();
        dto.setEmail("invalid-email");

        Set<ConstraintViolation<TeacherRegistration>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Should fail when password is too short")
    void teacherRegistration_ShortPassword() {
        TeacherRegistration dto = validRegistration();
        dto.setPassword("123");

        Set<ConstraintViolation<TeacherRegistration>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Should fail when experience is negative")
    void teacherRegistration_NegativeExperience() {
        TeacherRegistration dto = validRegistration();
        dto.setYearOfExperience(-2);

        Set<ConstraintViolation<TeacherRegistration>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("yearOfExperience"));
    }

    @Test
    @DisplayName("Should fail when required string fields exceed max size")
    void teacherRegistration_TooLongFields() {
        TeacherRegistration dto = validRegistration();
        dto.setFirstName("a".repeat(300));
        dto.setLastName("b".repeat(300));
        dto.setBio("c".repeat(2000));

        Set<ConstraintViolation<TeacherRegistration>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bio"));
    }

    @Test
    @DisplayName("Should pass with valid TeacherRegistration")
    void teacherRegistration_ValidInput() {
        TeacherRegistration dto = validRegistration();
        Set<ConstraintViolation<TeacherRegistration>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    // --- TeacherLogin Tests ---

    @Test
    @DisplayName("Should fail when fields are blank in TeacherLogin")
    void teacherLogin_BlankFields() {
        TeacherLogin dto = TeacherLogin.builder().build();

        Set<ConstraintViolation<TeacherLogin>> violations = validator.validate(dto);
        assertThat(violations).hasSize(2);
    }

    @Test
    @DisplayName("Should fail with invalid email in TeacherLogin")
    void teacherLogin_InvalidEmail() {
        TeacherLogin dto = TeacherLogin.builder()
                .email("invalid")
                .password("validPassword")
                .build();

        Set<ConstraintViolation<TeacherLogin>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Should pass with valid TeacherLogin")
    void teacherLogin_ValidInput() {
        TeacherLogin dto = TeacherLogin.builder()
                .email("valid@example.com")
                .password("validPassword")
                .build();

        Set<ConstraintViolation<TeacherLogin>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    // --- Helper for valid registration DTO ---
    private TeacherRegistration validRegistration() {
        return TeacherRegistration.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("0712345678")
                .password("SecurePass123")
                .professionalLevel("Senior")
                .certification("Certified")
                .profilePicture("profile.jpg")
                .bio("Experienced teacher in sciences.")
                .yearOfExperience(5)
                .course("Physics")
                .language("English")
                .build();
    }
}
