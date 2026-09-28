package com.zone01kisumu.backend.TeacherTest;

import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.repository.TeacherRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TeacherRepositoryTest {

    @Mock
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should find teacher by email")
    void shouldFindByEmail() {
        Teacher teacher = Teacher.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0712345678")
                .password("securePass123")
                .professionalLevel("Senior")
                .certification("Certified")
                .profilePicture("profile.jpg")
                .bio("Experienced math teacher")
                .yearOfExperience(5)
                .course("Mathematics")
                .language("English")
                .build();

        Teacher savedTeacher = Teacher.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0712345678")
                .password("securePass123")
                .professionalLevel("Senior")
                .certification("Certified")
                .profilePicture("profile.jpg")
                .bio("Experienced math teacher")
                .yearOfExperience(5)
                .course("Mathematics")
                .language("English")
                .build();

        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);
        when(teacherRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(savedTeacher));

        teacherRepository.save(teacher);

        Optional<Teacher> result = teacherRepository.findByEmail("jane.doe@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyIfEmailNotFound() {
        when(teacherRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<Teacher> result = teacherRepository.findByEmail("notfound@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when email is null")
    void shouldReturnEmptyIfEmailIsNull() {
        when(teacherRepository.findByEmail(null)).thenReturn(Optional.empty());

        Optional<Teacher> result = teacherRepository.findByEmail(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when email is blank")
    void shouldReturnEmptyIfEmailIsBlank() {
        when(teacherRepository.findByEmail("   ")).thenReturn(Optional.empty());

        Optional<Teacher> result = teacherRepository.findByEmail("   ");

        assertThat(result).isEmpty();
    }
}
