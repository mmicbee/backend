package com.zone01kisumu.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CompletedLessonEntry;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CourseProgressResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonCompletionResultDTO;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonStatusResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.MarkCompleteRequest;
import com.zone01kisumu.backend.service.LessonCompletionService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing student lesson completions and progress tracking.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class LessonCompletionController {

    private final LessonCompletionService lessonCompletionService;

    /**
     * Marks a lesson as completed for a student.
     *
     * @param studentId ID of the student.
     * @param lessonId  ID of the lesson.
     * @param request   Optional request body with custom timestamp.
     * @return 200 OK on success, 404 if not found, 409 if already completed.
     */
    @PostMapping("/{studentId}/lessons/{lessonId}/complete")
    public ResponseEntity<LessonCompletionResultDTO> markComplete(
            @PathVariable Long studentId,
            @PathVariable Long lessonId,
            @RequestBody(required = false) MarkCompleteRequest request) {
        try {
            LessonCompletionResultDTO result = lessonCompletionService.markComplete(
                    studentId,
                    lessonId,
                    request != null ? request.getTimestamp() : null
            );
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Unmarks a completed lesson (resets status).
     *
     * @param studentId ID of the student.
     * @param lessonId  ID of the lesson.
     * @return 204 No Content on success, 404 if record not found.
     */
    @DeleteMapping("/{studentId}/lessons/{lessonId}/complete")
    public ResponseEntity<Void> unmarkComplete(
            @PathVariable Long studentId,
            @PathVariable Long lessonId) {
        try {
            lessonCompletionService.unmarkComplete(studentId, lessonId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the completion status for a student and lesson.
     *
     * @param studentId ID of the student.
     * @param lessonId  ID of the lesson.
     * @return LessonStatusResponse.
     */
    @GetMapping("/{studentId}/lessons/{lessonId}/status")
    public ResponseEntity<LessonStatusResponse> getStatus(
            @PathVariable Long studentId,
            @PathVariable Long lessonId) {
        try {
            LessonStatusResponse status = lessonCompletionService.getStatus(studentId, lessonId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all completed lessons for a student in a course.
     *
     * @param studentId ID of the student.
     * @param courseId  ID of the course.
     * @return List of CompletedLessonEntry.
     */
    @GetMapping("/{studentId}/courses/{courseId}/completed-lessons")
    public ResponseEntity<List<CompletedLessonEntry>> getCompletedLessons(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            List<CompletedLessonEntry> entries = lessonCompletionService.getCompletedLessons(studentId, courseId);
            return ResponseEntity.ok(entries);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves overall course completion progress for a student.
     *
     * @param studentId ID of the student.
     * @param courseId  ID of the course.
     * @return CourseProgressResponse.
     */
    @GetMapping("/{studentId}/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            CourseProgressResponse progress = lessonCompletionService.getCourseProgress(studentId, courseId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
