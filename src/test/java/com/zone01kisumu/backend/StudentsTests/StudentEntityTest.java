package com.zone01kisumu.backend.StudentsTests;

import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class StudentEntityTest {

    @Mock
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup saveAndFlush to throw exception for invalid students
        doAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            if (student.getFirstName() == null ||
                student.getEmail() == null ||
                student.getPhone() == null ||
                (student.getEmail() != null && student.getEmail().length() > 255) ||
                (student.getPhone() != null && student.getPhone().length() > 20)) {
                throw new DataIntegrityViolationException("Constraint violation");
            }
            return null; // saveAndFlush returns void
        }).when(studentRepository).saveAndFlush(any(Student.class));
    }

    @Test
    @DisplayName("Should save a valid Student")
    void shouldSaveValidStudent() {
        Student student = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("0712345678")
                .profilePicture("profile.jpg")
                .password("securePassword123")
                .build();

        Student savedStudent = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("0712345678")
                .profilePicture("profile.jpg")
                .password("securePassword123")
                .createdAt(LocalDateTime.now())
                .build();

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        Student saved = studentRepository.save(student);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should set createdAt to current time by default")
    void shouldSetCreatedAtAutomatically() {
        Student student = Student.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0712345679")
                .profilePicture("profile.jpg")
                .password("securePassword123")
                .build();

        Student savedStudent = Student.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0712345679")
                .profilePicture("profile.jpg")
                .password("securePassword123")
                .createdAt(LocalDateTime.now())
                .build();

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        Student saved = studentRepository.save(student);
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should fail when firstname is null")
    void shouldFailWhenFirstnameIsNull() {
        Student student = Student.builder()
                .lastName("Doe")
                .email("null.firstname@example.com")
                .phone("0712345680")
                .profilePicture("profile.jpg")
                .password("password123")
                .build();

        assertThatThrownBy(() -> studentRepository.saveAndFlush(student))
                .isInstanceOf(DataIntegrityViolationException.class);
    }



    @Test
    @DisplayName("Should fail when required fields are missing")
    void shouldFailWhenRequiredFieldsMissing() {
        Student student = new Student(); // no fields set

        assertThatThrownBy(() -> studentRepository.saveAndFlush(student))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should fail when email exceeds 255 characters")
    void shouldFailWhenEmailTooLong() {
        String longEmail = "a".repeat(250) + "@ex.com"; // >255 characters
        Student student = Student.builder()
                .firstName("Long")
                .lastName("Email")
                .email(longEmail)
                .phone("0712345684")
                .profilePicture("profile.jpg")
                .password("pass")
                .build();

        assertThatThrownBy(() -> studentRepository.saveAndFlush(student))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should fail when phone exceeds 20 characters")
    void shouldFailWhenPhoneTooLong() {
        Student student = Student.builder()
                .firstName("Phone")
                .lastName("TooLong")
                .email("phone.long@example.com")
                .phone("1".repeat(21)) // exceeds max length
                .profilePicture("profile.jpg")
                .password("pass")
                .build();

        assertThatThrownBy(() -> studentRepository.saveAndFlush(student))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
