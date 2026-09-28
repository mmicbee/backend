package com.zone01kisumu.backend.studentAttendanceTests;

import com.zone01kisumu.backend.dto.StudentAttendanceDTO;
import com.zone01kisumu.backend.controller.CourseProgressController;
import com.zone01kisumu.backend.dto.CourseProgressDTOs;
import com.zone01kisumu.backend.service.CourseProgressService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Course Progress Controller  Test
class CourseProgressControllerTest {

    @Mock
    private CourseProgressService courseProgressService;

    @InjectMocks
    private CourseProgressController courseProgressController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMarkAttendance() {
        // Arrange
        StudentAttendanceDTO requestDto = StudentAttendanceDTO.builder()
                .studentId(1L)
                .courseLessonId(3L)
                .attendanceStatus("PRESENT")
                .markedBy("TEACHER")
                .build();

        StudentAttendanceDTO returnedDto = StudentAttendanceDTO.builder()
                .id(100L)
                .studentId(1L)
                .courseLessonId(3L)
                .attendanceStatus("PRESENT")
                .markedBy("TEACHER")
                .build();

        when(courseProgressService.markAttendance(requestDto)).thenReturn(returnedDto);

        // Act
        ResponseEntity<StudentAttendanceDTO> response = courseProgressController.markAttendance(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StudentAttendanceDTO responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");

        assertEquals(100L, responseBody.getId());
        verify(courseProgressService).markAttendance(requestDto);
    }

    @Test
    void testGetStudentProgressForTopic() {
        Long studentId = 1L;
        Long topicId = 2L;

        CourseProgressDTOs dto = CourseProgressDTOs.builder()
                .id(50L)
                .studentId(studentId)
                .topicId(topicId)
                .courseId(10L)
                .status("IN_PROGRESS")
                .progressPercentage(50.0)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(courseProgressService.getStudentProgressForTopic(studentId, topicId)).thenReturn(dto);

        ResponseEntity<CourseProgressDTOs> response = courseProgressController.getStudentProgressForTopic(studentId,
                topicId);

        CourseProgressDTOs body = Objects.requireNonNull(response.getBody(), "Response body should not be null");

        assertEquals(50L, body.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(courseProgressService).getStudentProgressForTopic(studentId, topicId);
    }

    @Test
    void testGetOverallProgress() {
        Long studentId = 1L;
        Long courseId = 2L;
        Double progress = 75.0;

        when(courseProgressService.getOverallCourseProgress(studentId, courseId)).thenReturn(progress);

        ResponseEntity<Double> response = courseProgressController.getOverallProgress(studentId, courseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(progress, response.getBody());
        verify(courseProgressService).getOverallCourseProgress(studentId, courseId);
    }
}
