package com.zone01kisumu.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.QuizScore;

@Repository
public interface QuizScoreRepository extends JpaRepository<QuizScore, Long> {

    List<QuizScore> findByStudentId(Long studentId);

    List<QuizScore> findByQuizId(Long quizId);

    List<QuizScore> findByCourseId(Long courseId);

    @Query("SELECT qs FROM QuizScore qs WHERE qs.studentId = :studentId "
            + "AND (:quizId IS NULL OR qs.quizId = :quizId) "
            + "AND (:courseId IS NULL OR qs.courseId = :courseId) "
            + "AND (CAST(:startDate AS timestamp) IS NULL OR qs.recordedAt >= :startDate) "
            + "AND (CAST(:endDate AS timestamp) IS NULL OR qs.recordedAt <= :endDate) "
            + "ORDER BY qs.recordedAt DESC")
    List<QuizScore> findStudentScoresFiltered(
            @Param("studentId") Long studentId,
            @Param("quizId") Long quizId,
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT qs FROM QuizScore qs WHERE qs.quizId = :quizId "
            + "AND (:studentId IS NULL OR qs.studentId = :studentId) "
            + "AND (CAST(:startDate AS timestamp) IS NULL OR qs.recordedAt >= :startDate) "
            + "AND (CAST(:endDate AS timestamp) IS NULL OR qs.recordedAt <= :endDate) "
            + "ORDER BY qs.recordedAt DESC")
    List<QuizScore> findQuizScoresFiltered(
            @Param("quizId") Long quizId,
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT qs FROM QuizScore qs WHERE qs.courseId = :courseId "
            + "AND (:studentId IS NULL OR qs.studentId = :studentId) "
            + "AND (:quizId IS NULL OR qs.quizId = :quizId) "
            + "AND (CAST(:startDate AS timestamp) IS NULL OR qs.recordedAt >= :startDate) "
            + "AND (CAST(:endDate AS timestamp) IS NULL OR qs.recordedAt <= :endDate) "
            + "ORDER BY qs.recordedAt DESC")
    List<QuizScore> findCourseScoresFiltered(
            @Param("courseId") Long courseId,
            @Param("studentId") Long studentId,
            @Param("quizId") Long quizId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
