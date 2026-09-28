package com.zone01kisumu.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.dto.QuizScoreDTOs.LogScoreRequest;
import com.zone01kisumu.backend.dto.QuizScoreDTOs.QuizScoreResponse;
import com.zone01kisumu.backend.service.QuizScoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for logging and retrieving quiz scores for students and instructors.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizScoreController {

    private final QuizScoreService quizScoreService;

    /**
     * Logs a student's quiz score.
     *
     * @param quizId  ID of the quiz.
     * @param request Payload containing student ID, score, and optional metadata.
     * @return 201 Created with QuizScoreResponse.
     */
    @PostMapping("/quizzes/{quizId}/scores")
    public ResponseEntity<QuizScoreResponse> logQuizScore(
            @PathVariable Long quizId,
            @Valid @RequestBody LogScoreRequest request) {
        QuizScoreResponse response = quizScoreService.logQuizScore(quizId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves quiz scores for a specific student, with optional filtering.
     *
     * @param studentId ID of the student.
     * @param quizId    Optional quiz ID filter.
     * @param courseId  Optional course ID filter.
     * @param startDate Optional starting date filter.
     * @param endDate   Optional ending date filter.
     * @return 200 OK with List of QuizScoreResponse.
     */
    @GetMapping("/students/{studentId}/quizzes/scores")
    public ResponseEntity<List<QuizScoreResponse>> getScoresByStudent(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long quizId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<QuizScoreResponse> responses = quizScoreService.getScoresByStudent(
                studentId, quizId, courseId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves scores for a specific quiz (instructor/course manager view).
     *
     * @param quizId    ID of the quiz.
     * @param studentId Optional student ID filter.
     * @param startDate Optional starting date filter.
     * @param endDate   Optional ending date filter.
     * @return 200 OK with List of QuizScoreResponse.
     */
    @GetMapping("/quizzes/{quizId}/scores")
    public ResponseEntity<List<QuizScoreResponse>> getScoresByQuiz(
            @PathVariable Long quizId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<QuizScoreResponse> responses = quizScoreService.getScoresByQuiz(
                quizId, studentId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves all quiz scores within a course (instructor/course analytics view).
     *
     * @param courseId  ID of the course.
     * @param studentId Optional student ID filter.
     * @param quizId    Optional quiz ID filter.
     * @param startDate Optional starting date filter.
     * @param endDate   Optional ending date filter.
     * @return 200 OK with List of QuizScoreResponse.
     */
    @GetMapping("/courses/{courseId}/quizzes/scores")
    public ResponseEntity<List<QuizScoreResponse>> getScoresByCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long quizId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<QuizScoreResponse> responses = quizScoreService.getScoresByCourse(
                courseId, studentId, quizId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }
}
