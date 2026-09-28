package com.zone01kisumu.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for logging and retrieving quiz scores.
 */
public class QuizScoreDTOs {

    /**
     * Request payload to record a student's quiz score.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogScoreRequest {
        @NotNull(message = "studentId is required")
        private Long studentId;

        @NotNull(message = "score is required")
        @DecimalMin(value = "0.0", message = "score cannot be negative")
        @DecimalMax(value = "1000.0", message = "score exceeds maximum allowed value")
        private Double score;

        private Long courseId;
        private Double maxScore;
        private LocalDateTime timestamp;
        private String feedback;
    }

    /**
     * Response payload representing a recorded quiz score.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizScoreResponse {
        private Long id;
        private Long studentId;
        private String studentName;
        private String studentEmail;
        private Long quizId;
        private Long courseId;
        private Double score;
        private Double maxScore;
        private Double percentage;
        private LocalDateTime timestamp;
        private String feedback;
        private LocalDateTime createdAt;
    }
}
