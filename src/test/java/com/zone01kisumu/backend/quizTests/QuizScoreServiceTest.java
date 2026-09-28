package com.zone01kisumu.backend.quizTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zone01kisumu.backend.dto.QuizScoreDTOs.LogScoreRequest;
import com.zone01kisumu.backend.dto.QuizScoreDTOs.QuizScoreResponse;
import com.zone01kisumu.backend.model.QuizScore;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.QuizScoreRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.service.QuizScoreService;

@ExtendWith(MockitoExtension.class)
class QuizScoreServiceTest {

    @Mock
    private QuizScoreRepository quizScoreRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private QuizScoreService quizScoreService;

    private Student testStudent;
    private QuizScore testQuizScore;

    @BeforeEach
    void setUp() {
        testStudent = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        testQuizScore = QuizScore.builder()
                .id(10L)
                .studentId(1L)
                .quizId(101L)
                .courseId(5L)
                .score(85.0)
                .maxScore(100.0)
                .recordedAt(LocalDateTime.now())
                .feedback("Good effort!")
                .build();
    }

    @Test
    void logQuizScore_Success() {
        LogScoreRequest request = LogScoreRequest.builder()
                .studentId(1L)
                .courseId(5L)
                .score(85.0)
                .maxScore(100.0)
                .feedback("Good effort!")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(quizScoreRepository.save(any(QuizScore.class))).thenReturn(testQuizScore);

        QuizScoreResponse response = quizScoreService.logQuizScore(101L, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getStudentId());
        assertEquals("John Doe", response.getStudentName());
        assertEquals("john.doe@example.com", response.getStudentEmail());
        assertEquals(101L, response.getQuizId());
        assertEquals(85.0, response.getScore());
        assertEquals(100.0, response.getMaxScore());
        assertEquals(85.0, response.getPercentage());
        assertEquals("Good effort!", response.getFeedback());
        verify(quizScoreRepository).save(any(QuizScore.class));
    }

    @Test
    void logQuizScore_ThrowsWhenQuizIdNull() {
        LogScoreRequest request = LogScoreRequest.builder().studentId(1L).score(80.0).build();
        assertThrows(IllegalArgumentException.class, () -> quizScoreService.logQuizScore(null, request));
    }

    @Test
    void logQuizScore_ThrowsWhenRequestNull() {
        assertThrows(IllegalArgumentException.class, () -> quizScoreService.logQuizScore(101L, null));
    }

    @Test
    void logQuizScore_ThrowsWhenStudentNotFound() {
        LogScoreRequest request = LogScoreRequest.builder().studentId(999L).score(80.0).build();
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> quizScoreService.logQuizScore(101L, request));
    }

    @Test
    void logQuizScore_ThrowsWhenScoreNegative() {
        LogScoreRequest request = LogScoreRequest.builder().studentId(1L).score(-5.0).build();
        assertThrows(IllegalArgumentException.class, () -> quizScoreService.logQuizScore(101L, request));
    }

    @Test
    void getScoresByStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(quizScoreRepository.findStudentScoresFiltered(1L, null, null, null, null))
                .thenReturn(List.of(testQuizScore));

        List<QuizScoreResponse> results = quizScoreService.getScoresByStudent(1L, null, null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(101L, results.get(0).getQuizId());
    }

    @Test
    void getScoresByQuiz_Success() {
        testQuizScore.setStudent(testStudent);
        when(quizScoreRepository.findQuizScoresFiltered(101L, null, null, null))
                .thenReturn(List.of(testQuizScore));

        List<QuizScoreResponse> results = quizScoreService.getScoresByQuiz(101L, null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(85.0, results.get(0).getScore());
    }

    @Test
    void getScoresByCourse_Success() {
        testQuizScore.setStudent(testStudent);
        when(quizScoreRepository.findCourseScoresFiltered(5L, null, null, null, null))
                .thenReturn(List.of(testQuizScore));

        List<QuizScoreResponse> results = quizScoreService.getScoresByCourse(5L, null, null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(5L, results.get(0).getCourseId());
    }
}
