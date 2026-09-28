package com.zone01kisumu.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;

// Repository for Course Topic persistence operations
@Repository
public interface CourseTopicRepository extends JpaRepository<CourseTopic, Long> {
    long countByCourse(Course course);
    
    List<CourseTopic> findByCourseId(Long courseId);

    List<CourseTopic> findByCourseIdAndTotalExpectedLessons(Long courseId, Integer totalExpectedLessons);
  
    @Query("SELECT SUM(co.duration) FROM CourseTopic co WHERE co.course.id = :courseId")
    Integer getTotalDurationByCourseId(@Param("courseId") Long courseId);
    
}
