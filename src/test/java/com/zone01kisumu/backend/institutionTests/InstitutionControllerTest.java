package com.zone01kisumu.backend.institutionTests;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.controller.InstitutionController;
import com.zone01kisumu.backend.dto.InstitutionLoginDTO;
import com.zone01kisumu.backend.dto.InstitutionRegistrationDTO;
import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.service.InstitutionService;

// Test class for InstitutionController
@ExtendWith(MockitoExtension.class)
class InstitutionControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private InstitutionController institutionController;

    private InstitutionRegistrationDTO registrationDTO;
    private InstitutionLoginDTO loginDTO;
    private Institution sampleInstitution;

    @BeforeEach
    void setUp() {
        registrationDTO = InstitutionRegistrationDTO.builder()
                .institutionName("Sample School")
                .registrationNumber("REG123")
                .schoolType("PRIMARY")
                .educationSystem("CBC")
                .location("Nairobi")
                .email("school@example.com")
                .phone("0700000000")
                .password("password123")
                .build();

        loginDTO = InstitutionLoginDTO.builder()
                .email("school@example.com")
                .password("password123")
                .build();

        sampleInstitution = Institution.builder()
                .institutionName("Sample School")
                .email("school@example.com")
                .phone("0700000000")
                .password("encodedPassword")
                .build();
    }

    @Test
void registerInstitution_success() {
    InstitutionRegistrationDTO dto = new InstitutionRegistrationDTO();
    dto.setEmail("test@example.com");

    Institution mockInstitution = new Institution();
    mockInstitution.setId(1L);
    mockInstitution.setEmail("test@example.com");
    mockInstitution.setPhone("1234567890");
    mockInstitution.setInstitutionName("Test School");
    mockInstitution.setRegistrationNumber("REG123");
    mockInstitution.setLocation("Nairobi");
    mockInstitution.setSchoolType(Institution.SchoolType.PRIMARY);
    mockInstitution.setEducationSystem(Institution.EducationSystem.CBC);

    when(institutionService.register(dto)).thenReturn(mockInstitution);

    ResponseEntity<?> response = institutionController.registerInstitution(dto);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    Object responseBodyObj = response.getBody();
    assertNotNull(responseBodyObj);
    assertInstanceOf(Map.class, responseBodyObj);

    @SuppressWarnings("unchecked")
    Map<String, Object> responseBody = (Map<String, Object>) responseBodyObj;

    assertTrue(responseBody.containsKey("message"));
    assertTrue(responseBody.containsKey("institution"));

    InstitutionRegistrationDTO expectedDto = InstitutionService.mapToRegistrationDTO(mockInstitution);
    assertEquals(expectedDto, responseBody.get("institution"));
}


    @Test
    void registerInstitution_shouldReturnBadRequestOnError() {
        when(institutionService.register(registrationDTO))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        ResponseEntity<?> response = institutionController.registerInstitution(registrationDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Object rawBody = response.getBody();
        if (rawBody == null) {
            throw new AssertionError("Expected response body to be non-null");
        }
        if (!(rawBody instanceof Map)) {
            throw new AssertionError("Expected response body to be a Map");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) rawBody;

        assertThat(body.get("error"))
                .isEqualTo("Institution registration failed: Email already in use");
    }

    @Test
    void loginInstitution_shouldReturnTokenAndInstitution() {
        when(institutionService.login(loginDTO)).thenReturn(sampleInstitution);
        when(institutionService.loadUserByUsername(loginDTO.getEmail()))
                .thenReturn(User.builder()
                        .username(loginDTO.getEmail())
                        .password("encodedPassword")
                        .authorities("INSTITUTION")
                        .build());

        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("mocked-jwt-token");

        ResponseEntity<?> response = institutionController.loginInstitution(loginDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Object rawBody = response.getBody();
        if (rawBody == null) {
            throw new AssertionError("Expected response body to be non-null");
        }
        if (!(rawBody instanceof Map)) {
            throw new AssertionError("Expected response body to be a Map");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) rawBody;

        assertThat(body.get("token")).isEqualTo("mocked-jwt-token");
        assertThat(body.get("institution")).isEqualTo(InstitutionService.mapToLoginDTO(sampleInstitution));
    }

    @Test
    void loginInstitution_shouldReturnUnauthorizedOnBadCredentials() {
        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<?> response = institutionController.loginInstitution(loginDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        Object rawBody = response.getBody();
        if (rawBody == null) {
            throw new AssertionError("Expected response body to be non-null");
        }
        if (!(rawBody instanceof Map)) {
            throw new AssertionError("Expected response body to be a Map");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) rawBody;

        assertThat(body.get("error")).isEqualTo("Invalid credentials");
    }

    @Test
    void handleException_shouldReturnBadRequestResponse() {
        Exception e = new RuntimeException("Something went wrong");

        ResponseEntity<String> response = institutionController.handleException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Error: Something went wrong");
    }
}
