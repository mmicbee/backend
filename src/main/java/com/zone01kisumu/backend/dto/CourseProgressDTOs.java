package com.zone01kisumu.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//the dto for the course progress for all the courses attendedrses attended
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseProgressDTOs {

    private Long id;

    @NotNull(message = "Student ID is required")
    @Min(value = 1, message = "Student ID must be a positive number.")
    private Long studentId;

    @NotNull(message = "Topic ID is required")
    @Min(value = 1, message = "Topic ID must be a positive number.")
    private Long topicId;

    @NotNull(message = "Course ID is required")
    @Min(value = 1, message = "Course ID must be a positive number.")
    private Long courseId;

    private String topicTitle;

    @Min(value = 0, message = "Completed lessons count must be zero or positive.")
    private Integer completedLessonsCount;

    @Min(value = 0, message = "Total expected lessons must be zero or positive.")
    private Integer totalExpectedLessons;

    @DecimalMin(value = "0.0", message = "Progress must be at least 0%.")
    @DecimalMax(value = "100.0", message = "Progress must not exceed 100%.")
    private Double progressPercentage;

    @NotBlank(message = "Status must not be blank.")
    private String status;

    @NotNull(message = "Last updated must not be null.")
    private LocalDateTime lastUpdated;
}
