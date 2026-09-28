package com.zone01kisumu.backend.componentTests;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.zone01kisumu.backend.component.JwtAuthenticationFilter;
import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.service.AppUserDetailsService;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AppUserDetailsService appUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
void testValidTokenSetsAuthentication() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath("/api/courses");  // ensure path not skipped by filter
    request.addHeader("Authorization", "Bearer test.jwt.token");

    MockHttpServletResponse response = new MockHttpServletResponse();

    UserDetails mockUser = new User(
        "testuser",
        "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")) // add authority
    );

    when(jwtUtil.extractUsername("test.jwt.token")).thenReturn("testuser");
    when(jwtUtil.extractRole("test.jwt.token")).thenReturn("ROLE_STUDENT");  // Add this line!
    when(jwtUtil.validateToken("test.jwt.token", mockUser)).thenReturn(true);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
}



    @Test
    void testNoAuthHeaderSkipsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(); // no header
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response); // still should pass through
    }

    @Test
    void testInvalidTokenDoesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractUsername("invalid.token")).thenThrow(new RuntimeException("Invalid"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response); // still goes through
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
