package com.zone01kisumu.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// class CourseTopicDTO is used to transfer course topic data between the service and controller layers
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseTopicDTO {

    private Long id;

    @NotBlank(message = "Topic title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 512, message = "Description must not exceed 512 characters")
    private String description;

    @NotNull(message = "Total expected lessons is required")
    @Min(value = 1, message = "Total expected lessons must be at least 1")
    private int totalExpectedLessons;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

}
