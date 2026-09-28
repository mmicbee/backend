package com.zone01kisumu.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for Lesson Completion and Student Course Progress.
 */
public class LessonCompletionDTOs {

    /**
     * Request payload to mark a lesson as complete.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkCompleteRequest {
        private LocalDateTime timestamp;
    }

    /**
     * Response payload representing completion status of a single lesson.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonStatusResponse {
        private boolean completed;
        private LocalDateTime completionDate;
    }

    /**
     * Response entry representing a completed lesson.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedLessonEntry {
        private Long lessonId;
        private String lessonName;
        private Long courseId;
        private Long topicId;
        private LocalDateTime completionDate;
        private String status;
    }

    /**
     * Response payload representing student progress across an entire course.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseProgressResponse {
        private Long studentId;
        private Long courseId;
        private long totalLessons;
        private long completedLessons;
        private double percentageCompleted;
    }

    /**
     * Response DTO returned after marking a lesson complete.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonCompletionResultDTO {
        private Long id;
        private Long studentId;
        private Long lessonId;
        private Long courseId;
        private boolean completed;
        private LocalDateTime completionDate;
        private String status;
        private String message;
    }
}
