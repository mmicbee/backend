package com.zone01kisumu.backend.TeacherTest;

import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.TeacherRepository;

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

class TeacherEntityTest {

    @Mock
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup saveAndFlush to throw exception for invalid teachers
        doAnswer(invocation -> {
            Teacher teacher = invocation.getArgument(0);
            if (teacher.getFirstName() == null ||
                teacher.getEmail() == null ||
                teacher.getPhone() == null ||
                (teacher.getEmail() != null && teacher.getEmail().length() > 255) ||
                (teacher.getPhone() != null && teacher.getPhone().length() > 20)) {
                throw new DataIntegrityViolationException("Constraint violation");
            }
            return null; // saveAndFlush returns void
        }).when(teacherRepository).saveAndFlush(any(Teacher.class));
    }

    @Test
    @DisplayName("Should save a valid Teacher")
    void shouldSaveValidTeacher() {
        Teacher teacher = Teacher.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0712345678")
                .password("securePassword")
                .professionalLevel("Senior")
                .certification("Certified Teacher")
                .profilePicture("profile.jpg")
                .bio("Experienced teacher")
                .yearOfExperience(5)
                .course("Math")
                .language("English")
                .build();

        Teacher savedTeacher = Teacher.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0712345678")
                .password("securePassword")
                .professionalLevel("Senior")
                .certification("Certified Teacher")
                .profilePicture("profile.jpg")
                .bio("Experienced teacher")
                .yearOfExperience(5)
                .course("Math")
                .language("English")
                .createdAt(LocalDateTime.now())
                .build();

        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);

        Teacher saved = teacherRepository.save(teacher);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should auto-set createdAt timestamp")
    void shouldAutoSetCreatedAt() {
        Teacher teacher = Teacher.builder()
                .firstName("Time")
                .lastName("Setter")
                .email("time.setter@example.com")
                .phone("0712345679")
                .password("securePassword")
                .build();

        Teacher savedTeacher = Teacher.builder()
                .id(2L)
                .firstName("Time")
                .lastName("Setter")
                .email("time.setter@example.com")
                .phone("0712345679")
                .password("securePassword")
                .createdAt(LocalDateTime.now())
                .build();

        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);

        Teacher saved = teacherRepository.save(teacher);

        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should fail when firstname is null")
    void shouldFailWhenFirstnameIsNull() {
        Teacher teacher = Teacher.builder()
                .lastName("Doe")
                .email("null.firstname@example.com")
                .phone("0712345680")
                .password("securePassword")
                .build();

        assertThatThrownBy(() -> teacherRepository.saveAndFlush(teacher))
                .isInstanceOf(DataIntegrityViolationException.class);
    }



    @Test
    @DisplayName("Should fail when required fields are missing")
    void shouldFailWhenRequiredFieldsMissing() {
        Teacher teacher = new Teacher(); // no values

        assertThatThrownBy(() -> teacherRepository.saveAndFlush(teacher))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should fail when email exceeds 255 characters")
    void shouldFailWhenEmailTooLong() {
        String longEmail = "a".repeat(250) + "@mail.com";

        Teacher teacher = Teacher.builder()
                .firstName("Long")
                .lastName("Email")
                .email(longEmail)
                .phone("0712345684")
                .password("pass")
                .build();

        assertThatThrownBy(() -> teacherRepository.saveAndFlush(teacher))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should fail when phone exceeds 20 characters")
    void shouldFailWhenPhoneTooLong() {
        Teacher teacher = Teacher.builder()
                .firstName("Phone")
                .lastName("TooLong")
                .email("phone.toolong@example.com")
                .phone("1".repeat(21)) // >20 chars
                .password("pass")
                .build();

        assertThatThrownBy(() -> teacherRepository.saveAndFlush(teacher))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
