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

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void testCorsConfigurationSource_defaultPatterns() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/courses");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertTrue(config.getAllowCredentials());
        assertTrue(config.getAllowedOriginPatterns().contains("http://localhost:*"));
        assertTrue(config.getAllowedOriginPatterns().contains("http://127.0.0.1:*"));
        assertTrue(config.getAllowedOriginPatterns().contains("https://lms-ujuzi.vercel.app"));
    }

    @Test
    void testCorsConfigurationSource_customOrigins() {
        securityConfig.setAllowedOrigins("https://example.com, https://another.com");
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/courses");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertTrue(config.getAllowedOriginPatterns().contains("https://example.com"));
        assertTrue(config.getAllowedOriginPatterns().contains("https://another.com"));
    }

    @Test
    void preflightFromDeployedFrontendReturnsCorsHeaders() throws Exception {
        CorsFilter filter = new CorsFilter(securityConfig.corsConfigurationSource());
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/courses");
        request.addHeader("Origin", "https://lms-ujuzi.vercel.app");
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Headers", "authorization,content-type");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        assertEquals("https://lms-ujuzi.vercel.app", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
        assertTrue(response.getHeader("Access-Control-Allow-Headers").contains("authorization"));
    }

    @Test
    void registrationPreflightFromDeployedFrontendReturnsCorsHeaders() throws Exception {
        CorsFilter filter = new CorsFilter(securityConfig.corsConfigurationSource());
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/student/register");
        request.addHeader("Origin", "https://lms-ujuzi.vercel.app");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "content-type");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        assertEquals("https://lms-ujuzi.vercel.app", response.getHeader("Access-Control-Allow-Origin"));
        assertTrue(response.getHeader("Access-Control-Allow-Methods").contains("POST"));
    }

    @Test
    void preflightFromUnknownOriginIsRejected() throws Exception {
        CorsFilter filter = new CorsFilter(securityConfig.corsConfigurationSource());
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/courses");
        request.addHeader("Origin", "https://another-site.vercel.app");
        request.addHeader("Access-Control-Request-Method", "GET");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }
}