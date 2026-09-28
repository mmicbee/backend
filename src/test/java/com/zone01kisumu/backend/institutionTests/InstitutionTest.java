package com.zone01kisumu.backend.institutionTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.Institution;

public class InstitutionTest {

    @Test
    public void testBuilderAndDefaults() {
        Institution institution = Institution.builder()
                .institutionName("Greenfield Academy")
                .registrationNumber("REG123")
                .schoolType(Institution.SchoolType.PRIMARY)
                .educationSystem(Institution.EducationSystem.CBC)
                .location("Nairobi")
                .email("info@greenfield.ac.ke")
                .phone("0712345678")
                .password("securepassword")
                .build();

        assertNotNull(institution);
        assertEquals("Greenfield Academy", institution.getInstitutionName());
        assertEquals("REG123", institution.getRegistrationNumber());
        assertEquals(Institution.SchoolType.PRIMARY, institution.getSchoolType());
        assertEquals(Institution.EducationSystem.CBC, institution.getEducationSystem());
        assertEquals("Nairobi", institution.getLocation());
        assertEquals("info@greenfield.ac.ke", institution.getEmail());
        assertEquals("0712345678", institution.getPhone());
        assertEquals("securepassword", institution.getPassword());

        // Defaults
        assertEquals(Institution.AccreditationStatus.PENDING, institution.getAccreditationStatus());
        assertNotNull(institution.getCreatedAt());
    }
}
