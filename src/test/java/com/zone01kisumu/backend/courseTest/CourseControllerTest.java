package com.zone01kisumu.backend.courseTest;

import com.zone01kisumu.backend.controller.CourseController;
import com.zone01kisumu.backend.dto.CourseDTO;
import com.zone01kisumu.backend.service.CourseService;

import org.checkerframework.checker.units.qual.t;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.mockito.InjectMocks;

// Test class for CourseController
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private CourseDTO sampleCourse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleCourse = new CourseDTO();
        sampleCourse.setId(1L);
        sampleCourse.setTitle("Intro to Java");
        sampleCourse.setDescription("Learn Java basics.");
        sampleCourse.setDuration(10);
        sampleCourse.setStartDate(LocalDateTime.now().plusDays(1));
        sampleCourse.setEndDate(LocalDateTime.now().plusDays(10));
        sampleCourse.setMode("ONLINE");
        sampleCourse.setPrice(new BigDecimal("199.99"));
        sampleCourse.setTeacherId(5L);
        sampleCourse.setPaymentMethod("MPESA");
        sampleCourse.setPaymentAccount("MPESA123");
        sampleCourse.setCreatedAt(LocalDateTime.now());
    }
    

    @Test
    void createCourse_shouldReturnCreatedCourse() {
        when(courseService.addCourse(any(CourseDTO.class))).thenReturn(sampleCourse);

        ResponseEntity<CourseDTO> response = courseController.createCourse(sampleCourse);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleCourse);
    }

    @Test
    void getCourseById_shouldReturnCourse_whenFound() {
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(sampleCourse));

        ResponseEntity<CourseDTO> response = courseController.getCourseById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleCourse);
    }

    @Test
    void getCourseById_shouldReturnNotFound_whenNotFound() {
        when(courseService.getCourseById(999L)).thenReturn(Optional.empty());

        ResponseEntity<CourseDTO> response = courseController.getCourseById(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAllCourses_shouldReturnListOfCourses() {
        List<CourseDTO> courses = Arrays.asList(sampleCourse);
        when(courseService.getAllCourses()).thenReturn(courses);

        ResponseEntity<List<CourseDTO>> response = courseController.getAllCourses();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(sampleCourse);
    }

    @Test
    void updateCourse_shouldReturnUpdatedCourse_whenFound() {
        when(courseService.updateCourse(eq(1L), any(CourseDTO.class))).thenReturn(Optional.of(sampleCourse));

        ResponseEntity<CourseDTO> response = courseController.updateCourse(1L, sampleCourse);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleCourse);
    }

    @Test
    void updateCourse_shouldReturnNotFound_whenNotExists() {
        when(courseService.updateCourse(eq(2L), any(CourseDTO.class))).thenReturn(Optional.empty());

        ResponseEntity<CourseDTO> response = courseController.updateCourse(2L, sampleCourse);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void deleteCourse_shouldReturnNoContent_whenDeleted() {
        when(courseService.deleteCourse(1L)).thenReturn(true);

        ResponseEntity<Void> response = courseController.deleteCourse(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteCourse_shouldReturnNotFound_whenNotFound() {
        when(courseService.deleteCourse(2L)).thenReturn(false);

        ResponseEntity<Void> response = courseController.deleteCourse(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getCoursesByTitle_shouldReturnCourses() {
        List<CourseDTO> courses = Arrays.asList(sampleCourse);
        when(courseService.getCoursesByTitleContaining("Java")).thenReturn(courses);
        ResponseEntity<List<CourseDTO>> response = courseController.getCoursesByTitle("Java");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(sampleCourse);
    }

    @Test
    void getCoursesByTitleOrDescriptionContaining_shouldReturnCourses() {
        List<CourseDTO> courses = Arrays.asList(sampleCourse);
        when(courseService.getCoursesByTitleOrDescriptionContaining("Java")).thenReturn(courses);
        ResponseEntity<List<CourseDTO>> response = courseController.getCoursesByTitleOrDescriptionContaining("Java");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(sampleCourse);
    }

    @Test
    void getCoursesByTeacherId_shouldReturnCourses() {
        List<CourseDTO> courses = Arrays.asList(sampleCourse);
        when(courseService.getCoursesByTeacherId(5L)).thenReturn(courses);
        ResponseEntity<List<CourseDTO>> response = courseController.getCoursesByTeacherId(5L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(sampleCourse);
    }

    @Test
    void getCoursesByPriceRange_shouldReturnCourses() {
        List<CourseDTO> courses = Arrays.asList(sampleCourse);
        when(courseService.getCoursesByPriceRange(100.0, 300.0)).thenReturn(courses);
        ResponseEntity<List<CourseDTO>> response = courseController.getCoursesByPriceRange("100.0", "300.0");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(sampleCourse);
    }

    @Test
    void getCoursesByPriceRange_shouldHandleEmptyResult() {
        when(courseService.getCoursesByPriceRange(500.0, 600.0)).thenReturn(Arrays.asList());
        ResponseEntity<List<CourseDTO>> response = courseController.getCoursesByPriceRange("500.0", "600.0");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void createCourse_shouldHandleServiceException() {
        when(courseService.addCourse(any(CourseDTO.class))).thenThrow(new RuntimeException("Service error"));
        try {
            courseController.createCourse(sampleCourse);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Service error");
        }
    }

    @Test
    void getCourseById_shouldHandleServiceException() {
        when(courseService.getCourseById(1L)).thenThrow(new RuntimeException("Service error"));
        try {
            courseController.getCourseById(1L);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Service error");
        }
    }
}
