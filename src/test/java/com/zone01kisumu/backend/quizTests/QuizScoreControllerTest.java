package com.zone01kisumu.backend.quizTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.zone01kisumu.backend.controller.QuizScoreController;
import com.zone01kisumu.backend.dto.QuizScoreDTOs.LogScoreRequest;
import com.zone01kisumu.backend.dto.QuizScoreDTOs.QuizScoreResponse;
import com.zone01kisumu.backend.service.QuizScoreService;

@ExtendWith(MockitoExtension.class)
class QuizScoreControllerTest {

    @Mock
    private QuizScoreService quizScoreService;

    @InjectMocks
    private QuizScoreController quizScoreController;

    private QuizScoreResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = QuizScoreResponse.builder()
                .id(1L)
                .studentId(2L)
                .studentName("Jane Doe")
                .studentEmail("jane@example.com")
                .quizId(10L)
                .courseId(5L)
                .score(90.0)
                .maxScore(100.0)
                .percentage(90.0)
                .timestamp(LocalDateTime.now())
                .feedback("Excellent work")
                .build();
    }

    @Test
    void logQuizScore_returnsCreated_onSuccess() {
        LogScoreRequest request = LogScoreRequest.builder()
                .studentId(2L)
                .score(90.0)
                .build();

        when(quizScoreService.logQuizScore(eq(10L), any(LogScoreRequest.class))).thenReturn(sampleResponse);

        ResponseEntity<QuizScoreResponse> response = quizScoreController.logQuizScore(10L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sampleResponse, response.getBody());
        verify(quizScoreService).logQuizScore(eq(10L), eq(request));
    }

    @Test
    void getScoresByStudent_returnsOk() {
        when(quizScoreService.getScoresByStudent(eq(2L), any(), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<List<QuizScoreResponse>> response = quizScoreController.getScoresByStudent(
                2L, null, null, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getScoresByQuiz_returnsOk() {
        when(quizScoreService.getScoresByQuiz(eq(10L), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<List<QuizScoreResponse>> response = quizScoreController.getScoresByQuiz(
                10L, null, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getScoresByCourse_returnsOk() {
        when(quizScoreService.getScoresByCourse(eq(5L), any(), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<List<QuizScoreResponse>> response = quizScoreController.getScoresByCourse(
                5L, null, null, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
