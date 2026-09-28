package com.zone01kisumu.backend.institutionTests;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.InstitutionRegistrationDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

// This test class is used to validate the InstitutionRegistrationDTO
// It checks if the DTO correctly enforces validation rules for institution registration
public class InstitutionRegistrationDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegistrationDTO() {
        InstitutionRegistrationDTO dto = InstitutionRegistrationDTO.builder()
                .institutionName("Test School")
                .registrationNumber("REG123")
                .schoolType("PRIMARY")
                .educationSystem("CBC")
                .location("Nairobi")
                .email("test@school.com")
                .phone("0712345678")
                .password("securepassword")
                .build();

        Set<ConstraintViolation<InstitutionRegistrationDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "DTO should be valid");
    }

    @Test
    void testInvalidRegistrationDTO_missingFields() {
        InstitutionRegistrationDTO dto = new InstitutionRegistrationDTO(); // All fields empty

        Set<ConstraintViolation<InstitutionRegistrationDTO>> violations = validator.validate(dto);

        assertEquals(8, violations.size(), "All fields should trigger validation errors");
    }

    @Test
    void testInvalidEmail() {
        InstitutionRegistrationDTO dto = InstitutionRegistrationDTO.builder()
                .institutionName("Test School")
                .registrationNumber("REG123")
                .schoolType("PRIMARY")
                .educationSystem("CBC")
                .location("Nairobi")
                .email("invalid-email")
                .phone("0712345678")
                .password("securepassword")
                .build();

        Set<ConstraintViolation<InstitutionRegistrationDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
}
