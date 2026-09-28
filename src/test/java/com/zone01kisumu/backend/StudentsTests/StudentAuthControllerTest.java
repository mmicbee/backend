package com.zone01kisumu.backend.StudentsTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.controller.StudentAuthController;
import com.zone01kisumu.backend.dto.AuthResponse;
import com.zone01kisumu.backend.dto.StudentLogin;
import com.zone01kisumu.backend.dto.StudentRegistration;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.service.StudentAuthService;

// test for the Students API endpoints controller test 
class StudentAuthControllerTest {

        private StudentAuthService studentAuthService;
        private JwtUtil jwtUtil;
        private StudentAuthController controller;

        private StudentRegistration validRegistration;
        private Student mockStudent;

        @BeforeEach
        void setUp() {
                studentAuthService = mock(StudentAuthService.class);
                jwtUtil = mock(JwtUtil.class);
                controller = new StudentAuthController(studentAuthService, jwtUtil);

                validRegistration = new StudentRegistration();
                validRegistration.setId(1L);
                validRegistration.setFirstName("Test");
                validRegistration.setLastName("User");
                validRegistration.setEmail("test.Student@example.com");
                validRegistration.setPhone("0700123456");
                validRegistration.setPassword("StrongPass123");

                mockStudent = new Student();
                mockStudent.setId(1L);
                mockStudent.setEmail(validRegistration.getEmail());
        }

        @Test
        void shouldRegisterStudentSuccessfully() {
                when(studentAuthService.registerStudent(any(StudentRegistration.class)))
                                .thenReturn(mockStudent);

                ResponseEntity<AuthResponse> response = controller.registerStudent(validRegistration);

                assertNotNull(response);
                AuthResponse body = response.getBody();
                assertNotNull(body);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertEquals("student registration successful!", body.getMessage());
                assertEquals(mockStudent.getId().toString(), body.getUserId());
        }

        @Test
        void shouldLoginSuccessfully() {
                when(studentAuthService.loginStudent(any(StudentLogin.class)))
                                .thenReturn(mockStudent);

                when(jwtUtil.generateToken(anyString(), eq("STUDENT")))
                                .thenReturn("fake-jwt-token");

                StudentLogin loginRequest = new StudentLogin(
                                validRegistration.getEmail(), validRegistration.getPassword());

                ResponseEntity<AuthResponse> response = controller.loginStudent(loginRequest);

                assertNotNull(response);
                AuthResponse body = response.getBody();
                assertNotNull(body);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("student login successful!", body.getMessage());
                assertEquals("fake-jwt-token", body.getToken());
                assertEquals(mockStudent.getId().toString(), body.getUserId());
        }

        @Test
        void shouldFailLoginWithWrongPassword() {
                StudentLogin loginRequest = new StudentLogin(validRegistration.getEmail(), "WrongPass");

                when(studentAuthService.loginStudent(any()))
                                .thenThrow(new RuntimeException("Unauthorized"));

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        controller.loginStudent(loginRequest);
                });

                assertEquals("Unauthorized", exception.getMessage());
        }

        @Test
        void shouldFailLoginWithNonExistentEmail() {
                StudentLogin loginRequest = new StudentLogin("nonexistent@example.com", "NoSuchPass123");

                when(studentAuthService.loginStudent(any()))
                                .thenThrow(new RuntimeException("Unauthorized"));

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        controller.loginStudent(loginRequest);
                });

                assertEquals("Unauthorized", exception.getMessage());
        }

        @Test
        void shouldFailOnMissingField() {
                validRegistration.setFirstName(null);

                when(studentAuthService.registerStudent(any()))
                                .thenThrow(new RuntimeException("Missing required field"));

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        controller.registerStudent(validRegistration);
                });
                assertEquals("Missing required field", exception.getMessage());
        }

        @Test
        void shouldDeleteStudentProfile() {
                doNothing().when(studentAuthService).deleteStudentProfile(anyLong());

                ResponseEntity<String> response = controller.deleteStudentProfile(1L);

                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("Student profile deleted successfully", response.getBody());
        }

        @Test
        void shouldFailDeleteStudentProfile() {
                doThrow(new RuntimeException("Student not found"))
                                .when(studentAuthService)
                                .deleteStudentProfile(1L);

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        controller.deleteStudentProfile(1L);
                });

                assertEquals("Student not found", exception.getMessage());
        }

        @Test
        void shouldUpdateStudentProfile() {
                Student updatedStudent = new Student();
                updatedStudent.setId(1L);
                updatedStudent.setEmail(validRegistration.getEmail());
                updatedStudent.setFirstName(validRegistration.getFirstName());
                updatedStudent.setLastName(validRegistration.getLastName());

                when(studentAuthService.updateStudentProfile(anyLong(), any()))
                                .thenReturn(updatedStudent);

                ResponseEntity<String> response = controller.updateStudentProfile(1L, validRegistration);

                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("Student profile updated successfully", response.getBody());
        }

        @Test
        void shouldFailUpdateStudentProfile() {
                when(studentAuthService.updateStudentProfile(anyLong(), any()))
                                .thenThrow(new RuntimeException("Student not found"));

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        controller.updateStudentProfile(1L, validRegistration);
                });

                assertEquals("Student not found", exception.getMessage());
        }

        //get student by id test success
        @Test
        void shouldGetStudentByIdSuccessfully() {
                when(studentAuthService.getStudentById(1L)).thenReturn(Optional.of(mockStudent));
                ResponseEntity<Student> response = controller.getStudentById(1L);
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(mockStudent, response.getBody());
        }

        //get student by id not found test
        @Test
        void shouldFailGetStudentByIdNotFound() {
                when(studentAuthService.getStudentById(2L)).thenReturn(Optional.empty());
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        controller.getStudentById(2L);
                });
                assertEquals("Student not found with id: 2", exception.getMessage());  
        }


}
