package com.zone01kisumu.backend.securityTests;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.InstitutionRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.repository.TeacherRepository;
import com.zone01kisumu.backend.service.InstitutionService;
import com.zone01kisumu.backend.service.StudentAuthService;
import com.zone01kisumu.backend.service.TeacherAuthService;

/**
 * RBAC unit and integration tests verifying Role-Based Access Control
 * for STUDENT, TEACHER, and INSTITUTION roles.
 */
@ExtendWith(MockitoExtension.class)
public class RbacSecurityTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentAuthService studentAuthService;

    @InjectMocks
    private TeacherAuthService teacherAuthService;

    @InjectMocks
    private InstitutionService institutionService;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret("12345678901234567890123456789012");
        jwtUtil.setExpiration(3600000L);
    }

    @Test
    void studentAuthService_loadUserByUsername_shouldReturnRoleStudent() {
        Student student = Student.builder()
                .id(1L)
                .email("student@ujuzi.com")
                .password("encodedPass")
                .build();

        when(studentRepository.findByEmail("student@ujuzi.com")).thenReturn(Optional.of(student));

        UserDetails userDetails = studentAuthService.loadUserByUsername("student@ujuzi.com");

        assertNotNull(userDetails);
        assertEquals("student@ujuzi.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_STUDENT"::equals));
    }

    @Test
    void teacherAuthService_loadUserByUsername_shouldReturnRoleTeacher() {
        Teacher teacher = Teacher.builder()
                .id(2L)
                .email("teacher@ujuzi.com")
                .password("encodedPass")
                .build();

        when(teacherRepository.findByEmail("teacher@ujuzi.com")).thenReturn(Optional.of(teacher));

        UserDetails userDetails = teacherAuthService.loadUserByUsername("teacher@ujuzi.com");

        assertNotNull(userDetails);
        assertEquals("teacher@ujuzi.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_TEACHER"::equals));
    }

    @Test
    void institutionService_loadUserByUsername_shouldReturnRoleInstitution() {
        Institution institution = Institution.builder()
                .id(3L)
                .email("admin@school.com")
                .password("encodedPass")
                .build();

        when(institutionRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(institution));

        UserDetails userDetails = institutionService.loadUserByUsername("admin@school.com");

        assertNotNull(userDetails);
        assertEquals("admin@school.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_INSTITUTION"::equals));
    }

    @Test
    void jwtUtil_shouldGenerateAndExtractCanonicalRolesForAllActorTypes() {
        // Student token
        String studentToken = jwtUtil.generateToken("student@ujuzi.com", "STUDENT");
        assertEquals("ROLE_STUDENT", jwtUtil.extractRole(studentToken));

        // Teacher token
        String teacherToken = jwtUtil.generateToken("teacher@ujuzi.com", "TEACHER");
        assertEquals("ROLE_TEACHER", jwtUtil.extractRole(teacherToken));

        // Institution token
        String instToken = jwtUtil.generateToken("admin@school.com", "INSTITUTION");
        assertEquals("ROLE_INSTITUTION", jwtUtil.extractRole(instToken));
    }

    @Test
    void jwtUtil_shouldAvoidDoubleRolePrefix() {
        String token1 = jwtUtil.generateToken("teacher@ujuzi.com", "ROLE_TEACHER");
        assertEquals("ROLE_TEACHER", jwtUtil.extractRole(token1));

        String token2 = jwtUtil.generateToken("student@ujuzi.com", "ROLE_STUDENT");
        assertEquals("ROLE_STUDENT", jwtUtil.extractRole(token2));

        String token3 = jwtUtil.generateToken("admin@school.com", "ROLE_INSTITUTION");
        assertEquals("ROLE_INSTITUTION", jwtUtil.extractRole(token3));
    }
}
