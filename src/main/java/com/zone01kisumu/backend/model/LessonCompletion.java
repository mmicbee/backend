package com.zone01kisumu.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing student lesson completion tracking.
 */
@Entity
@Table(
        name = "lesson_completion",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_lesson_completion",
                columnNames = {"student_id", "lesson_id"}
        ),
        indexes = {
                @Index(name = "idx_completion_student_course", columnList = "student_id, course_id"),
                @Index(name = "idx_completion_student_lesson", columnList = "student_id, lesson_id")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, insertable = false, updatable = false)
    private Student student;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, insertable = false, updatable = false)
    private CourseLesson lesson;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, insertable = false, updatable = false)
    private Course course;

    @Column(name = "completion_date", nullable = false)
    private LocalDateTime completionDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CompletionStatus status = CompletionStatus.COMPLETED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CompletionStatus {
        COMPLETED,
        IN_PROGRESS
    }
}
