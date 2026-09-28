package com.zone01kisumu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.LessonCompletion;

/**
 * Spring Data JPA Repository for LessonCompletion entity.
 */
@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {

    Optional<LessonCompletion> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<LessonCompletion> findByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByStudentIdAndCourseId(Long studentId, Long courseId);

    void deleteByStudentIdAndLessonId(Long studentId, Long lessonId);
}
