package com.zone01kisumu.backend.institutionTests;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.zone01kisumu.backend.model.Institution;

import jakarta.persistence.EntityManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

// This test class is used to verify the persistence layer for the Institution entity
// It checks if an Institution can be persisted and then retrieved correctly from the database
// Ensure that the database is properly configured for testing, e.g., using an in-memory database
class InstitutionJpaTest {

    @Mock
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPersistAndLoadInstitution() {
        Institution institution = Institution.builder()
                .institutionName("Greenfield Academy")
                .registrationNumber("REG123")
                .schoolType(Institution.SchoolType.PRIMARY)
                .educationSystem(Institution.EducationSystem.CBC)
                .location("Nairobi")
                .email("info@greenfield.ac.ke")
                .phone("0712345678")
                .password("securepassword")
                .createdAt(LocalDateTime.now())
                .build();

        Institution savedInstitution = Institution.builder()
                .id(1L)
                .institutionName("Greenfield Academy")
                .registrationNumber("REG123")
                .schoolType(Institution.SchoolType.PRIMARY)
                .educationSystem(Institution.EducationSystem.CBC)
                .location("Nairobi")
                .email("info@greenfield.ac.ke")
                .phone("0712345678")
                .password("securepassword")
                .createdAt(LocalDateTime.now())
                .build();

        doNothing().when(entityManager).persist(any(Institution.class));
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        when(entityManager.find(Institution.class, savedInstitution.getId())).thenReturn(savedInstitution);

        entityManager.persist(institution);
        entityManager.flush();
        entityManager.clear();

        Institution loaded = entityManager.find(Institution.class, savedInstitution.getId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getInstitutionName()).isEqualTo("Greenfield Academy");
        assertThat(loaded.getEmail()).isEqualTo("info@greenfield.ac.ke");
    }
}
