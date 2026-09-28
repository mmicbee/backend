package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.dto.CourseLessonDTO;
import com.zone01kisumu.backend.service.CourseLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/topics/{topicId}/lessons")
@RequiredArgsConstructor
@Slf4j
public class CourseLessonController {

    private final CourseLessonService courseLessonService;

    /**
     * Create a new lesson
     * User only provides: lessonName and ONE of (file, recordedLink, liveUrl)
     * System automatically computes: mediaType, duration, durationUnit, fileSize
     *
     * Example usage:
     * - For uploaded video: POST with lessonName + file
     * - For YouTube link: POST with lessonName + recordedLink (JSON)
     * - For live session: POST with lessonName + liveUrl (JSON)
     *
     * @param courseId ID of the course.
     * @param topicId ID of the course topic.
     * @param dto Lesson details including lessonName.
     * @param file Optional file upload (video/pdf/slides).
     * @return Created lesson details with ID and metadata.
     */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseLessonDTO> createLesson(
            @PathVariable Long courseId,
            @PathVariable Long topicId,
            @RequestPart("lesson") CourseLessonDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            dto.setCourseId(courseId);
            dto.setTopicId(topicId);
            CourseLessonDTO created = courseLessonService.createLesson(courseId, topicId, dto, file);
            log.info("Created lesson: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("File processing error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

     // Alternative endpoint for JSON-only requests (links/live URLs)
    @PostMapping(value = "/link", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CourseLessonDTO> createLessonWithLink(
            @PathVariable Long courseId,
            @PathVariable Long topicId,
            @RequestBody CourseLessonDTO dto) {
        try {
            dto.setCourseId(courseId);
            dto.setTopicId(topicId);
            CourseLessonDTO created = courseLessonService.createLesson(courseId, topicId, dto, null);
            log.info("Created lesson with link: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Processing error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

     // Update existing lesson
    @PutMapping(value = "/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseLessonDTO> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long topicId,
            @PathVariable Long lessonId,
            @RequestPart("lesson") CourseLessonDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            dto.setCourseId(courseId);
            dto.setTopicId(topicId);
            CourseLessonDTO updated = courseLessonService.updateLesson(lessonId, dto, file);
            log.info("Updated lesson: {}", lessonId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("File processing error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

     // Alternative update endpoint for JSON-only requests
    @PutMapping(value = "/{lessonId}/link", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CourseLessonDTO> updateLessonWithLink(
            @PathVariable Long courseId,
            @PathVariable Long topicId,
            @PathVariable Long lessonId,
            @RequestBody CourseLessonDTO dto) {
        try {
            dto.setCourseId(courseId);
            dto.setTopicId(topicId);
            CourseLessonDTO updated = courseLessonService.updateLesson(lessonId, dto, null);
            log.info("Updated lesson with link: {}", lessonId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Processing error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

     // Get all lessons for a specific topic
    @GetMapping
    public ResponseEntity<List<CourseLessonDTO>> getLessonsByTopic(
            @PathVariable Long courseId,
            @PathVariable Long topicId) {
        List<CourseLessonDTO> lessons = courseLessonService.getLessonsByTopicId(topicId);
        return ResponseEntity.ok(lessons);
    }

     // Get specific lesson by ID
    @GetMapping("/{lessonId}")
    public ResponseEntity<CourseLessonDTO> getLessonById(
            @PathVariable Long courseId,
            @PathVariable Long topicId,
            @PathVariable Long lessonId) {
        try {
            CourseLessonDTO lesson = courseLessonService.getLessonById(lessonId);
            return ResponseEntity.ok(lesson);
        } catch (IllegalArgumentException e) {
            log.error("Lesson not found: {}", lessonId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves and serves the content of a specific lesson for inline viewing.
     * This endpoint is designed to stream lesson content (like videos or PDFs) directly in the browser
     * rather than forcing a download. It sets the Content-Disposition header to "inline".
     *
     * @param courseId ID of the course, used for context in the URL.
     * @param topicId  ID of the topic, used for context in the URL.
     * @param lessonId ID of the lesson for which to retrieve the content.
     * @return A {@link ResponseEntity} containing the byte array of the content and HTTP headers
     *         configured for inline display. Returns 404 if the content is not found.
     */
    @GetMapping("/{lessonId}/content")
    public ResponseEntity<byte[]> getLessonContent(
            @PathVariable Long courseId,
            @PathVariable Long topicId,
            @PathVariable Long lessonId) {
        try {
            byte[] content = courseLessonService.getLessonContent(lessonId);
            CourseLessonDTO lesson = courseLessonService.getLessonById(lessonId);

            HttpHeaders headers = new HttpHeaders();

            // Set content type based on media type
            String mediaType = determineMediaType(lesson.getRecordedMediaType());
            headers.setContentType(MediaType.parseMediaType(mediaType));
            headers.setContentLength(content.length);
            headers.setContentDisposition(
                    ContentDisposition.inline()
                            .filename(lesson.getLessonName() + getFileExtension(lesson.getRecordedMediaType()))
                            .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (IllegalArgumentException e) {
            log.error("Content not found for lesson: {}", lessonId);
            return ResponseEntity.notFound().build();
        }
    }

     // Delete lesson
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long courseId,
            @PathVariable Long topicId,
            @PathVariable Long lessonId) {
        try {
            courseLessonService.deleteLesson(lessonId);
            log.info("Deleted lesson: {}", lessonId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Lesson not found: {}", lessonId);
            return ResponseEntity.notFound().build();
        }
    }

    // Get all lessons for a course (across all topics)
    @GetMapping("/all")
    public ResponseEntity<List<CourseLessonDTO>> getAllLessonsForCourse(
            @PathVariable Long courseId) {
        List<CourseLessonDTO> lessons = courseLessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    // HELPER METHODS
    private String determineMediaType(String recordedMediaType) {
        if (recordedMediaType == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return switch (recordedMediaType.toUpperCase()) {
            case "VIDEO" -> "video/mp4";
            case "PDF" -> "application/pdf";
            case "SLIDES" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    private String getFileExtension(String recordedMediaType) {
        if (recordedMediaType == null) {
            return "";
        }

        return switch (recordedMediaType.toUpperCase()) {
            case "VIDEO" -> ".mp4";
            case "PDF" -> ".pdf";
            case "SLIDES" -> ".pptx";
            default -> "";
        };
    }
}