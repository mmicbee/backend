package com.zone01kisumu.backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.CourseProgress;
import com.zone01kisumu.backend.model.Student;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {
    Optional<CourseProgress> findByStudentAndTopic(Student student, CourseTopic topic);

    boolean existsByStudentIdAndTopicId(Long studentId, Long topicId);

    // Custom query to get progress percentages for a student across all Topics in
    // a course
    @Query("SELECT cp.progressPercentage FROM CourseProgress cp WHERE cp.student = :student AND cp.course = :course")
    List<Double> findProgressPercentagesByStudentAndCourse(@Param("student") Student student,
            @Param("course") Course course);

}
