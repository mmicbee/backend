package com.zone01kisumu.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for Class, Student, and Course Performance Summary Reports.
 */
public class PerformanceReportDTOs {

    /**
     * Performance summary for a specific class or cohort/topic.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassPerformanceSummaryDTO {
        private Long classId;
        private String className;
        private Long courseId;
        private String courseTitle;
        private int totalStudents;
        private int activeStudents;
        private int totalLessons;
        private int completedLessonsTotal;
        private double averageCompletionRate;
        private double averageAttendanceRate;
        private double engagementScore;
        private List<StudentClassMetricDTO> studentSummaries;
        private LocalDateTime dateGenerated;
    }

    /**
     * Individual student metrics within a class summary report.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentClassMetricDTO {
        private Long studentId;
        private String studentName;
        private String email;
        private int completedLessons;
        private double completionRate;
        private double attendanceRate;
        private String status;
    }

    /**
     * Aggregated performance summary for a specific student across all courses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentPerformanceSummaryDTO {
        private Long studentId;
        private String studentName;
        private String email;
        private int totalCoursesEnrolled;
        private int completedCoursesCount;
        private int inProgressCoursesCount;
        private double overallCompletionRate;
        private int totalLessonsCompleted;
        private double attendanceRate;
        private double engagementScore;
        private List<StudentCourseMetricDTO> courseBreakdowns;
        private LocalDateTime dateGenerated;
    }

    /**
     * Performance metric for a single course in a student's summary report.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentCourseMetricDTO {
        private Long courseId;
        private String courseTitle;
        private int totalLessons;
        private int completedLessons;
        private double completionRate;
        private double attendanceRate;
        private String status;
    }

    /**
     * Aggregated performance summary for a specific course across all students.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePerformanceSummaryDTO {
        private Long courseId;
        private String courseTitle;
        private Long teacherId;
        private int totalEnrolledStudents;
        private int totalLessons;
        private int totalTopics;
        private double averageCompletionRate;
        private double overallAttendanceRate;
        private double engagementScore;
        private int studentsCompletedCount;
        private int studentsInProgressCount;
        private int studentsNotStartedCount;
        private List<TopicPerformanceMetricDTO> topicSummaries;
        private LocalDateTime dateGenerated;
    }

    /**
     * Performance metric for a topic within a course summary report.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicPerformanceMetricDTO {
        private Long topicId;
        private String topicTitle;
        private int expectedLessons;
        private int completedLessonsTotal;
        private double completionRate;
    }

    /**
     * Generic paginated envelope for reporting endpoints.
     *
     * @param <T> Content payload type.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceReportPageResponse<T> {
        private List<T> content;
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean isLast;
    }
}
