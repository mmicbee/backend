package com.zone01kisumu.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// New DTO for marking Student Attendance
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentAttendanceDTO {
    private Long id;

    @NotNull(message = "Student ID is required")
    @Min(value = 1, message = "Student ID must be a positive number.")
    private Long studentId;

    @NotNull(message = "Course Lesson ID is required")
    @Min(value = 1, message = "Course Lesson ID must be a positive number.")
    private Long courseLessonId;

    @NotBlank(message = "Attendance status is required (PRESENT, ABSENT, EXCUSED)")
    private String attendanceStatus; // Will be mapped to AttendanceStatus enum

    @NotBlank(message = "Marked by is required (TEACHER, SYSTEM)")
    private String markedBy; // Will be mapped to MarkedBy enum
}
