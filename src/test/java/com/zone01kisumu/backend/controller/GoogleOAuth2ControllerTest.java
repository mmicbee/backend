package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.service.InstitutionService;
import com.zone01kisumu.backend.service.StudentAuthService;
import com.zone01kisumu.backend.service.TeacherAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoogleOAuth2ControllerTest {

    @Mock
    private StudentAuthService studentAuthService;

    @Mock
    private TeacherAuthService teacherAuthService;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private GoogleOAuth2Controller googleOAuth2Controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(googleOAuth2Controller, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(googleOAuth2Controller, "oauthCallbackFullUrl", "http://localhost:5173/oauth/callback");
    }

    @Test
    void googleLogin() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        googleOAuth2Controller.googleLogin("student", request, response);
        assertEquals("student", request.getSession().getAttribute("role"));
        assertEquals("/oauth2/authorization/google", response.getRedirectedUrl());
    }

    @Test
    void user_student() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute("role", "student");

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("student@example.com");

        Student student = new Student();
        student.setId(1L);
        student.setEmail("student@example.com");
        when(studentAuthService.findByEmail("student@example.com")).thenReturn(java.util.Optional.of(student));
        when(jwtUtil.generateToken("student@example.com", "STUDENT")).thenReturn("student_token");

        googleOAuth2Controller.user(principal, request, response);

        String redirectedUrl = response.getRedirectedUrl();
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectedUrl).build().getQueryParams();

        assertEquals("student_token", queryParams.getFirst("token"));
        assertEquals("1", queryParams.getFirst("userId"));
        assertEquals("student", queryParams.getFirst("role"));
        assertEquals("http://localhost:5173/oauth/callback", redirectedUrl.split("\\?")[0]);
    }

    @Test
    void user_teacher() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute("role", "teacher");

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("teacher@example.com");

        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setEmail("teacher@example.com");
        when(teacherAuthService.findByEmail("teacher@example.com")).thenReturn(java.util.Optional.of(teacher));
        when(jwtUtil.generateToken("teacher@example.com", "TEACHER")).thenReturn("teacher_token");

        googleOAuth2Controller.user(principal, request, response);

        String redirectedUrl = response.getRedirectedUrl();
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectedUrl).build().getQueryParams();

        assertEquals("teacher_token", queryParams.getFirst("token"));
        assertEquals("1", queryParams.getFirst("userId"));
        assertEquals("teacher", queryParams.getFirst("role"));
        assertEquals("http://localhost:5173/oauth/callback", redirectedUrl.split("\\?")[0]);
    }

    @Test
    void user_institution() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute("role", "institution");

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("institution@example.com");

        Institution institution = new Institution();
        institution.setId(1L);
        institution.setEmail("institution@example.com");
        when(institutionService.findByEmail("institution@example.com")).thenReturn(java.util.Optional.of(institution));
        when(jwtUtil.generateToken("institution@example.com", "INSTITUTION")).thenReturn("institution_token");

        googleOAuth2Controller.user(principal, request, response);

        String redirectedUrl = response.getRedirectedUrl();
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectedUrl).build().getQueryParams();

        assertEquals("institution_token", queryParams.getFirst("token"));
        assertEquals("1", queryParams.getFirst("userId"));
        assertEquals("institution", queryParams.getFirst("role"));
        assertEquals("http://localhost:5173/oauth/callback", redirectedUrl.split("\\?")[0]);
    }

    @Test
    void user_invalidRole() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute("role", "invalid");

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("test@example.com");

        googleOAuth2Controller.user(principal, request, response);

        String redirectedUrl = response.getRedirectedUrl();
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectedUrl).build().getQueryParams();

        assertEquals("Invalid role specified", queryParams.getFirst("error"));
        assertEquals("http://localhost:5173/oauth/callback", redirectedUrl.split("\\?")[0]);
    }
}
