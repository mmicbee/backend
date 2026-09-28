package com.zone01kisumu.backend.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.*;

// Tracks a student's attendance for a specific scheduled CourseLesson.
@Entity
@Table(name = "student_attendance", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id",
        "course_lesson_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_lesson_id", nullable = false)
    private CourseLesson courseLesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "marked_by", nullable = false)
    private MarkedBy markedBy;

    @CreationTimestamp
    @Column(name = "marked_at", nullable = false, updatable = false)
    private LocalDateTime markedAt;

    public enum AttendanceStatus {
        PRESENT, ABSENT, EXCUSED
    }

    public enum MarkedBy {
        TEACHER, SYSTEM
    }
}
