package com.zone01kisumu.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zone01kisumu.backend.dto.QuizScoreDTOs.LogScoreRequest;
import com.zone01kisumu.backend.dto.QuizScoreDTOs.QuizScoreResponse;
import com.zone01kisumu.backend.model.QuizScore;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.QuizScoreRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service managing quiz score logging, score validation, and querying.
 */
@Service
@RequiredArgsConstructor
public class QuizScoreService {

    private final QuizScoreRepository quizScoreRepository;
    private final StudentRepository studentRepository;

    /**
     * Records a quiz score for a given student and quiz.
     *
     * @param quizId  ID of the quiz.
     * @param request Payload containing student ID, score, and metadata.
     * @return QuizScoreResponse containing the saved record.
     */
    @Transactional
    public QuizScoreResponse logQuizScore(Long quizId, LogScoreRequest request) {
        if (quizId == null) {
            throw new IllegalArgumentException("quizId cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (request.getStudentId() == null) {
            throw new IllegalArgumentException("studentId is required");
        }
        if (request.getScore() == null || request.getScore() < 0) {
            throw new IllegalArgumentException("Score must be non-negative");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found with ID: " + request.getStudentId()));

        double maxScore = (request.getMaxScore() != null && request.getMaxScore() > 0)
                ? request.getMaxScore()
                : 100.0;

        LocalDateTime timestamp = request.getTimestamp() != null
                ? request.getTimestamp()
                : LocalDateTime.now();

        QuizScore quizScore = QuizScore.builder()
                .studentId(student.getId())
                .quizId(quizId)
                .courseId(request.getCourseId())
                .score(request.getScore())
                .maxScore(maxScore)
                .recordedAt(timestamp)
                .feedback(request.getFeedback())
                .build();

        QuizScore saved = quizScoreRepository.save(quizScore);
        LoggerUtil.logInfo("Recorded quiz score for studentId={}, quizId={}, score={}/{}",
                student.getId(), quizId, saved.getScore(), saved.getMaxScore());

        return toResponseDTO(saved, student);
    }

    /**
     * Retrieves quiz scores for a specific student, with optional filters.
     *
     * @param studentId ID of the student.
     * @param quizId    Optional quiz ID filter.
     * @param courseId  Optional course ID filter.
     * @param startDate Optional starting date filter.
     * @param endDate   Optional ending date filter.
     * @return List of QuizScoreResponse.
     */
    @Transactional(readOnly = true)
    public List<QuizScoreResponse> getScoresByStudent(
            Long studentId, Long quizId, Long courseId, LocalDateTime startDate, LocalDateTime endDate) {
        if (studentId == null) {
            throw new IllegalArgumentException("studentId cannot be null");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        List<QuizScore> scores = quizScoreRepository.findStudentScoresFiltered(
                studentId, quizId, courseId, startDate, endDate);

        return scores.stream()
                .map(score -> toResponseDTO(score, student))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves quiz scores for a specific quiz, with optional filters (e.g. instructor view).
     *
     * @param quizId    ID of the quiz.
     * @param studentId Optional student ID filter.
     * @param startDate Optional starting date filter.
     * @param endDate   Optional ending date filter.
     * @return List of QuizScoreResponse.
     */
    @Transactional(readOnly = true)
    public List<QuizScoreResponse> getScoresByQuiz(
            Long quizId, Long studentId, LocalDateTime startDate, LocalDateTime endDate) {
        if (quizId == null) {
            throw new IllegalArgumentException("quizId cannot be null");
        }
        List<QuizScore> scores = quizScoreRepository.findQuizScoresFiltered(
                quizId, studentId, startDate, endDate);

        return scores.stream()
                .map(score -> {
                    Student student = score.getStudent() != null
                            ? score.getStudent()
                            : studentRepository.findById(score.getStudentId()).orElse(null);
                    return toResponseDTO(score, student);
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves quiz scores for an entire course, with optional filters.
     *
     * @param courseId  ID of the course.
     * @param studentId Optional student ID filter.
     * @param quizId    Optional quiz ID filter.
     * @param startDate Optional starting date filter.
     * @param endDate   Optional ending date filter.
     * @return List of QuizScoreResponse.
     */
    @Transactional(readOnly = true)
    public List<QuizScoreResponse> getScoresByCourse(
            Long courseId, Long studentId, Long quizId, LocalDateTime startDate, LocalDateTime endDate) {
        if (courseId == null) {
            throw new IllegalArgumentException("courseId cannot be null");
        }
        List<QuizScore> scores = quizScoreRepository.findCourseScoresFiltered(
                courseId, studentId, quizId, startDate, endDate);

        return scores.stream()
                .map(score -> {
                    Student student = score.getStudent() != null
                            ? score.getStudent()
                            : studentRepository.findById(score.getStudentId()).orElse(null);
                    return toResponseDTO(score, student);
                })
                .collect(Collectors.toList());
    }

    private QuizScoreResponse toResponseDTO(QuizScore score, Student student) {
        Double maxScore = score.getMaxScore() != null && score.getMaxScore() > 0 ? score.getMaxScore() : 100.0;
        double percentage = (score.getScore() != null && maxScore > 0)
                ? Math.round((score.getScore() / maxScore) * 10000.0) / 100.0
                : 0.0;

        String studentName = null;
        String studentEmail = null;
        if (student != null) {
            studentName = (student.getFirstName() != null ? student.getFirstName() : "")
                    + " " + (student.getLastName() != null ? student.getLastName() : "");
            studentName = studentName.trim();
            studentEmail = student.getEmail();
        }

        return QuizScoreResponse.builder()
                .id(score.getId())
                .studentId(score.getStudentId())
                .studentName(studentName)
                .studentEmail(studentEmail)
                .quizId(score.getQuizId())
                .courseId(score.getCourseId())
                .score(score.getScore())
                .maxScore(maxScore)
                .percentage(percentage)
                .timestamp(score.getRecordedAt())
                .feedback(score.getFeedback())
                .createdAt(score.getCreatedAt())
                .build();
    }
}
