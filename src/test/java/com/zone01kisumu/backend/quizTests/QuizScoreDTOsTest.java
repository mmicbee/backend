package com.zone01kisumu.backend.quizTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.QuizScoreDTOs.LogScoreRequest;
import com.zone01kisumu.backend.dto.QuizScoreDTOs.QuizScoreResponse;

class QuizScoreDTOsTest {

    @Test
    void testLogScoreRequestDTO() {
        LocalDateTime now = LocalDateTime.now();
        LogScoreRequest request = LogScoreRequest.builder()
                .studentId(5L)
                .courseId(12L)
                .score(88.0)
                .maxScore(100.0)
                .timestamp(now)
                .feedback("Good work")
                .build();

        assertEquals(5L, request.getStudentId());
        assertEquals(12L, request.getCourseId());
        assertEquals(88.0, request.getScore());
        assertEquals(100.0, request.getMaxScore());
        assertEquals(now, request.getTimestamp());
        assertEquals("Good work", request.getFeedback());
    }

    @Test
    void testQuizScoreResponseDTO() {
        LocalDateTime now = LocalDateTime.now();
        QuizScoreResponse response = QuizScoreResponse.builder()
                .id(1L)
                .studentId(2L)
                .studentName("John Doe")
                .studentEmail("john@example.com")
                .quizId(3L)
                .courseId(4L)
                .score(75.0)
                .maxScore(100.0)
                .percentage(75.0)
                .timestamp(now)
                .feedback("Passed")
                .createdAt(now)
                .build();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getStudentName());
        assertEquals("john@example.com", response.getStudentEmail());
        assertEquals(75.0, response.getPercentage());
    }
}
