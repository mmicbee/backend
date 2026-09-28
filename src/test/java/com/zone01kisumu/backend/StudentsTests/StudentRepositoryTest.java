package com.zone01kisumu.backend.StudentsTests;


import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class StudentRepositoryTest {

    @Mock
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should find Student by email")
    void shouldFindByEmail() {
        Student student = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("0712345678")
                .profilePicture("profile.jpg")
                .password("secret")
                .build();

        Student savedStudent = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("0712345678")
                .profilePicture("profile.jpg")
                .password("secret")
                .build();

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);
        when(studentRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(savedStudent));

        studentRepository.save(student);

        Optional<Student> result = studentRepository.findByEmail("john.doe@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyIfEmailNotFound() {
        when(studentRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<Student> result = studentRepository.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when email is null")
    void shouldReturnEmptyIfEmailIsNull() {
        when(studentRepository.findByEmail(null)).thenReturn(Optional.empty());

        Optional<Student> result = studentRepository.findByEmail(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when email is blank")
    void shouldReturnEmptyIfEmailIsBlank() {
        when(studentRepository.findByEmail("   ")).thenReturn(Optional.empty());

        Optional<Student> result = studentRepository.findByEmail("   ");

        assertThat(result).isEmpty();
    }
}
