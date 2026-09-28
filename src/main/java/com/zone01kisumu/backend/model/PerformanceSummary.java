package com.zone01kisumu.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an aggregated performance summary report for a class,
 * student, or course.
 */
@Entity
@Table(
        name = "performance_summary",
        indexes = {
                @Index(name = "idx_report_type_entity", columnList = "report_type, entity_id"),
                @Index(name = "idx_date_generated", columnList = "date_generated")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false, unique = true, length = 64)
    private String reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "date_generated", nullable = false)
    private LocalDateTime dateGenerated;

    @Column(name = "total_students")
    private Integer totalStudents;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    @Column(name = "completed_lessons")
    private Integer completedLessons;

    @Column(name = "completion_rate")
    private Double completionRate;

    @Column(name = "attendance_rate")
    private Double attendanceRate;

    @Column(name = "engagement_score")
    private Double engagementScore;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ReportType {
        CLASS,
        STUDENT,
        COURSE
    }
}
