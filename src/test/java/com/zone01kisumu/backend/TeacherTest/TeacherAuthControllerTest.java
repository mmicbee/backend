package com.zone01kisumu.backend.TeacherTest;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.controller.TeacherAuthController;
import com.zone01kisumu.backend.dto.AuthResponse;
import com.zone01kisumu.backend.dto.TeacherLogin;
import com.zone01kisumu.backend.dto.TeacherRegistration;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.service.TeacherAuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

// test for the teachers API endpoints controller test
// test for the teachers API endpoints controller test
public class TeacherAuthControllerTest {

    private TeacherAuthService teacherAuthService;
    private JwtUtil jwtUtil;
    private TeacherAuthController controller;
    private TeacherRegistration validRegistration;
    private Teacher mockTeacher;

    @BeforeEach
    void setUp() {
        teacherAuthService = mock(TeacherAuthService.class);
        jwtUtil = mock(JwtUtil.class);
        controller = new TeacherAuthController(teacherAuthService, jwtUtil);

        validRegistration = new TeacherRegistration();
        validRegistration.setFirstName("Jane");
        validRegistration.setLastName("Doe");
        validRegistration.setEmail("jane.teacher@example.com");
        validRegistration.setPhone("0712345678");
        validRegistration.setPassword("SecurePass123");
        validRegistration.setProfessionalLevel("Expert");
        validRegistration.setCertification("Certified Educator");
        validRegistration.setProfilePicture("profile.jpg");
        validRegistration.setBio("Passionate about teaching.");
        validRegistration.setYearOfExperience(5);
        validRegistration.setCourse("Mathematics");
        validRegistration.setLanguage("English");

        mockTeacher = new Teacher();
        mockTeacher.setId(1L);
        mockTeacher.setEmail(validRegistration.getEmail());
    }

    @Test
    void shouldRegisterTeacherSuccessfully() {
        when(teacherAuthService.registerTeacher(any())).thenReturn(mockTeacher);

        ResponseEntity<AuthResponse> response = controller.registerTeacher(validRegistration);
        assertNotNull(response);
        AuthResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Teacher registration successful!", body.getMessage());
        assertEquals(mockTeacher.getId().toString(), body.getUserId());
    }

    @Test
    void shouldFailOnDuplicateEmail() {
        when(teacherAuthService.registerTeacher(any()))
                .thenThrow(new RuntimeException("Email already registered"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.registerTeacher(validRegistration);
        });
        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void shouldFailOnMissingField() {
        validRegistration.setFirstName(null); // Simulate missing field

        when(teacherAuthService.registerTeacher(any()))
                .thenThrow(new RuntimeException("An unexpected error occurred"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.registerTeacher(validRegistration);
        });
        assertEquals("An unexpected error occurred", exception.getMessage());
    }

    @Test
    void shouldLoginSuccessfully() {
        when(teacherAuthService.loginTeacher(any())).thenReturn(mockTeacher);
        when(jwtUtil.generateToken(anyString(), eq("TEACHER")))
                .thenReturn("fake-jwt-token");
        TeacherLogin login = new TeacherLogin(validRegistration.getEmail(), validRegistration.getPassword());

        ResponseEntity<AuthResponse> response = controller.loginTeacher(login);
        assertNotNull(response);
        AuthResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Teacher login successful!", body.getMessage());
        assertEquals("fake-jwt-token", body.getToken());
        assertEquals(mockTeacher.getId().toString(), body.getUserId());
    }

    @Test
    void shouldFailLoginWithWrongPassword() {
        when(teacherAuthService.loginTeacher(any()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        TeacherLogin login = new TeacherLogin(validRegistration.getEmail(), "WrongPassword");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.loginTeacher(login);
        });
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void shouldFailLoginWithNonExistentEmail() {
        when(teacherAuthService.loginTeacher(any()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        TeacherLogin login = new TeacherLogin("nonexistent@teacher.com", "SomePassword");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.loginTeacher(login);
        });
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void shouldUpdateTeacherProfileSuccessfully() {
        when(teacherAuthService.updateTeacherProfile(eq(1L), any(TeacherRegistration.class)))
                .thenReturn(mockTeacher);

        ResponseEntity<String> response = controller.updateTeacherProfile(1L, validRegistration);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Teacher profile updated successfully", response.getBody());
    }

    @Test
    void shouldDeleteTeacherProfileSuccessfully() {
        doNothing().when(teacherAuthService).deleteTeacherProfile(1L);

        ResponseEntity<String> response = controller.deleteTeacherProfile(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Teacher profile deleted successfully", response.getBody());
    }

    @Test
    void shouldFailUpdateTeacherProfileWithNonExistentId() {
        when(teacherAuthService.updateTeacherProfile(eq(2L), any(TeacherRegistration.class)))
                .thenThrow(new RuntimeException("Teacher not found with id: 2"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.updateTeacherProfile(2L, validRegistration);
        });
        assertEquals("Teacher not found with id: 2", exception.getMessage());
    }

    @Test
    void shouldFailDeleteTeacherProfileWithNonExistentId() {
        doThrow(new RuntimeException("Teacher not found with id: 2"))
                .when(teacherAuthService).deleteTeacherProfile(2L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.deleteTeacherProfile(2L);
        });

        assertEquals("Teacher not found with id: 2", exception.getMessage());
    }

    //get teacher by id test
    @Test
    void shouldGetTeacherByIdSuccessfully() {
        when(teacherAuthService.getTeacherById(1L)).thenReturn(Optional.of(mockTeacher));

        ResponseEntity<Teacher> response = controller.getTeacherById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockTeacher, response.getBody());
    }

    //get teacher by id not found test
    @Test
    void shouldFailGetTeacherByIdNotFound() {
        when(teacherAuthService.getTeacherById(2L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.getTeacherById(2L);
        });
        assertEquals("Teacher not found with id: 2", exception.getMessage());
    }
}
