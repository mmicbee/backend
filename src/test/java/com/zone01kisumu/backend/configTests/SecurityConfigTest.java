package com.zone01kisumu.backend.configTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01kisumu.backend.component.JwtAuthenticationFilter;
import com.zone01kisumu.backend.config.SecurityConfig;
import com.zone01kisumu.backend.service.InstitutionService;
import com.zone01kisumu.backend.service.StudentAuthService;
import com.zone01kisumu.backend.service.TeacherAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StudentAuthService studentAuthService;

    @Mock
    private TeacherAuthService teacherAuthService;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private ObjectMapper objectMapper;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityConfig = new SecurityConfig(
                jwtAuthenticationFilter,
                passwordEncoder,
                studentAuthService,
                teacherAuthService,
                institutionService,
                objectMapper
        );
    }

    @Test
    void testSecurityFilterChainBean() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);

        // Mock the chained calls on HttpSecurity
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.headers(any())).thenReturn(httpSecurity);
        when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(JwtAuthenticationFilter.class), any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mockChain);

        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);

        assertNotNull(chain);

        verify(httpSecurity).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}