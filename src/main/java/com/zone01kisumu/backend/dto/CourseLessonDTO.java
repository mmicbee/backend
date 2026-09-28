package com.zone01kisumu.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// @JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseLessonDTO {

    private Long id;

    @NotNull(message = "Course ID is required")
    @Min(value = 1, message = "Course ID must be a positive number.")
    private Long courseId;

    @NotNull(message = "Topic ID is required")
    @Min(value = 1, message = "Topic ID must be a positive number.")
    private Long topicId;

    @NotBlank(message = "Lesson name is mandatory")
    private String lessonName;

    // For RECORDED lessons
    private String recordedMediaType; // "PDF", "VIDEO", "SLIDES", "OTHER"
    private String recordedLink;
    private byte[] recordedMedia;

    // For LIVE lessons
    private String liveUrl;
    
    private LocalDateTime lessonDate;

    private Integer duration; // Duration or size metric (meaning varies by type)
    private String durationUnit; // 'minutes', 'pages', 'words', 'slides', etc.
    private Long fileSize; // File size in bytes
    private String mediaType; // MIME type of the uploaded file

}
