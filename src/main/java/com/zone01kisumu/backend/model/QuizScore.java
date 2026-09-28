package com.zone01kisumu.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing student quiz scores and performance history.
 */
@Entity
@Table(
        name = "quiz_score",
        indexes = {
                @Index(name = "idx_quiz_score_student", columnList = "student_id"),
                @Index(name = "idx_quiz_score_quiz", columnList = "quiz_id"),
                @Index(name = "idx_quiz_score_course", columnList = "course_id")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, insertable = false, updatable = false)
    private Student student;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Builder.Default
    @Column(name = "max_score")
    private Double maxScore = 100.0;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
