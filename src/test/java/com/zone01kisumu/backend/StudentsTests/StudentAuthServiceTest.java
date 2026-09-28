package com.zone01kisumu.backend.StudentsTests;

import com.zone01kisumu.backend.dto.StudentLogin;
import com.zone01kisumu.backend.dto.StudentRegistration;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.service.StudentAuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//test for the Students auth controllers
class StudentAuthServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentAuthService studentAuthService;

    private Student student;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        student = Student.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phone("0712345678")
                .profilePicture("profile.jpg")
                .password("encodedPass")
                .build();
    }

    @Test
    void registerStudent_Success() {
        StudentRegistration registration = new StudentRegistration(1l, "Test", "User", "test@example.com", "0712345678", "profile.jpg", "plainPass");

        when(studentRepository.findByEmail(registration.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPass")).thenReturn("encodedPass");
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student result = studentAuthService.registerStudent(registration);

        assertEquals("test@example.com", result.getEmail());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void registerStudent_EmailAlreadyExists() {
        StudentRegistration registration = new StudentRegistration(1l, "Test", "User", "test@example.com", "0712345678", "profile.jpg", "plainPass");

        when(studentRepository.findByEmail(registration.getEmail())).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> studentAuthService.registerStudent(registration));
    }

    @Test
    void loginStudent_Success() {
        StudentLogin login = new StudentLogin("test@example.com", "plainPass");

        when(studentRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("plainPass", "encodedPass")).thenReturn(true);

        Student result = studentAuthService.loginStudent(login);

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void loginStudent_EmailNotFound() {
        StudentLogin login = new StudentLogin("wrong@example.com", "pass");

        when(studentRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> studentAuthService.loginStudent(login));
    }

    @Test
    void loginStudent_InvalidPassword() {
        StudentLogin login = new StudentLogin("test@example.com", "wrongPass");

        when(studentRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> studentAuthService.loginStudent(login));
    }

    @Test
    void loadUserByUsername_Success() {
        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));

        var userDetails = studentAuthService.loadUserByUsername("test@example.com");

        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPass", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(studentRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> studentAuthService.loadUserByUsername("missing@example.com"));
    }

    @Test
    void findByEmail_Found() {
        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        Optional<Student> result = studentAuthService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        when(studentRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<Student> result = studentAuthService.findByEmail("notfound@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_InvalidEmail() {
        when(studentRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        Optional<Student> result = studentAuthService.findByEmail("invalid@example.com");

        assertTrue(result.isEmpty());
    }
    // test delete by id
    @Test
    void deleteById_Success() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        studentAuthService.deleteStudentProfile(1L);
        verify(studentRepository).deleteById(1L);
    }

    @Test
    void deleteById_NotFound() {
        when(studentRepository.existsById(2L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> studentAuthService.deleteStudentProfile(2L));
    }

    // test update by id
    @Test
    void updateById_Success() {
        StudentRegistration registration = new StudentRegistration( 1l , "New", "Name", "new@example.com", "0711111111", "new.jpg", "newPass");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        studentAuthService.updateStudentProfile(1L, registration);

        verify(studentRepository).save(any(Student.class));
        assertEquals("New", student.getFirstName());
        assertEquals("new@example.com", student.getEmail());
    }

    @Test
    void updateById_NotFound() {
        StudentRegistration registration = new StudentRegistration(1l, "Test", "User", "test@example.com", "0712345678", "profile.jpg", "plainPass");
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> studentAuthService.updateStudentProfile(2L, registration));

    }

     //test get student by id when student does not exist
        @Test
        void testGetStudentById_notFound() {
                Long studentId = 2L;
                when(studentRepository.findById(studentId)).thenReturn(Optional.empty());
                Optional<Student> result = studentAuthService.getStudentById(studentId);
                assertFalse(result.isPresent());
        }
        //test get student by id when student exists
        @Test
        void shouldGetStudentDetailsById() {
                Long studentId = 1L;
                when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

                Optional<Student> result = studentAuthService.getStudentById(studentId);

                assertTrue(result.isPresent());
        }

        //findByPhone test
        @Test
        void shouldFindByPhoneSuccessfully() {
                when(studentRepository.findByPhone("0712345678")).thenReturn(Optional.of(student));
                Optional<Student> result = studentAuthService.findByPhone("0712345678");
                assertTrue(result.isPresent());
                assertEquals("0712345678", result.get().getPhone());
        }

}
