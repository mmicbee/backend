package com.zone01kisumu.backend.quizTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.QuizScore;
import com.zone01kisumu.backend.model.Student;

class QuizScoreEntityTest {

    @Test
    void testQuizScoreEntityCreationAndGettersSetters() {
        Student student = Student.builder().id(1L).firstName("Alice").lastName("Wonder").build();
        LocalDateTime now = LocalDateTime.now();

        QuizScore score = QuizScore.builder()
                .id(1L)
                .studentId(1L)
                .student(student)
                .quizId(20L)
                .courseId(10L)
                .score(95.5)
                .maxScore(100.0)
                .recordedAt(now)
                .feedback("Well done!")
                .build();

        assertNotNull(score);
        assertEquals(1L, score.getId());
        assertEquals(1L, score.getStudentId());
        assertEquals(student, score.getStudent());
        assertEquals(20L, score.getQuizId());
        assertEquals(10L, score.getCourseId());
        assertEquals(95.5, score.getScore());
        assertEquals(100.0, score.getMaxScore());
        assertEquals(now, score.getRecordedAt());
        assertEquals("Well done!", score.getFeedback());

        // Test toString and equals/hashCode coverage
        assertTrue(score.toString().contains("score=95.5"));
    }

    @Test
    void testQuizScoreNoArgsConstructor() {
        QuizScore score = new QuizScore();
        score.setId(2L);
        score.setScore(80.0);
        score.setMaxScore(100.0);

        assertEquals(2L, score.getId());
        assertEquals(80.0, score.getScore());
        assertEquals(100.0, score.getMaxScore());
    }
}
