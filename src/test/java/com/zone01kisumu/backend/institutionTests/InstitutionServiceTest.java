package com.zone01kisumu.backend.institutionTests;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zone01kisumu.backend.dto.InstitutionLoginDTO;
import com.zone01kisumu.backend.dto.InstitutionRegistrationDTO;
import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.repository.InstitutionRepository;
import com.zone01kisumu.backend.service.InstitutionService;

// Test class for InstitutionService
// This class tests the registration and login functionalities of the InstitutionService
class InstitutionServiceTest {

    @InjectMocks
    private InstitutionService institutionService;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    /*
     * Initialize mocks before each test
     * This method is called before each test method to set up the mocks
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // test register method that it saves an institution when valid input is
    // provided
    @Test
    void register_ShouldSaveInstitution_WhenValidInput() {
        InstitutionRegistrationDTO dto = InstitutionRegistrationDTO.builder()
                .institutionName("Test School")
                .registrationNumber("123")
                .schoolType("PRIMARY")
                .educationSystem("CBC")
                .location("Kisumu")
                .email("test@example.com")
                .phone("0700000000")
                .build();

        when(institutionRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(institutionRepository.existsByPhone(dto.getPhone())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");

        Institution savedInstitution = Institution.builder().id(1L).email(dto.getEmail()).build();
        when(institutionRepository.save(any(Institution.class))).thenReturn(savedInstitution);

        Institution result = institutionService.register(dto);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    // test register method that it throws an exception when phone is already in use
    @Test
    void register_ShouldThrowException_WhenPhoneAlreadyInUse() {
        InstitutionRegistrationDTO dto = InstitutionRegistrationDTO.builder()
                .institutionName("Test School")
                .registrationNumber("123")
                .schoolType("PRIMARY")
                .educationSystem("CBC")
                .location("Kisumu")
                .email("test@example.com")
                .phone("0700000000")
                .build();

        when(institutionRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(institutionRepository.existsByPhone(dto.getPhone())).thenReturn(true); // simulate phone already used

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            institutionService.register(dto);
        });

        assertEquals("Phone number already in use", ex.getMessage());
    }

    @Test
    void login_ShouldReturnInstitution_WhenCredentialsAreCorrect() {
        InstitutionLoginDTO loginDTO = InstitutionLoginDTO.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Institution institution = Institution.builder()
                .email("test@example.com")
                .password("hashedPassword")
                .build();

        when(institutionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(institution));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        Institution result = institutionService.login(loginDTO);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenInvalidPassword() {
        InstitutionLoginDTO loginDTO = InstitutionLoginDTO.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Institution institution = Institution.builder()
                .email("test@example.com")
                .password("correct")
                .build();

        when(institutionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(institution));
        when(passwordEncoder.matches("wrong", "correct")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> institutionService.login(loginDTO));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    // test loadUserByUsername that it returns a UserDetails object with the correct
    // username, password, and authorities
    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenEmailExists() {
        Institution institution = Institution.builder()
                .email("test@example.com")
                .password("hashedPassword")
                .build();

        when(institutionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(institution));

        UserDetails userDetails = institutionService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size()); // INSTITUTION
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenEmailNotFound() {
        when(institutionRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Exception ex = assertThrows(UsernameNotFoundException.class, () -> {
            institutionService.loadUserByUsername("missing@example.com");
        });

        assertEquals("Institution not found", ex.getMessage());
    }

    @Test
    void findByEmail_ShouldReturnInstitution_WhenEmailExists() {
        Institution institution = Institution.builder()
                .email("test@example.com")
                .build();

        when(institutionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(institution));

        Optional<Institution> result = institutionService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertNotNull(result.get());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_ShouldThrowException_WhenEmailNotFound() {
        when(institutionRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
    }

}