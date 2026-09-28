package com.zone01kisumu.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.CourseLesson;

@Repository
public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {
    
    // Find all lessons for a specific course
    List<CourseLesson> findByCourseId(Long courseId);
    
    // Find all lessons for a specific topic
    List<CourseLesson> findByTopicId(Long topicId);
    
    //Find lessons by media type
    List<CourseLesson> findByRecordedMediaType(CourseLesson.RecordedMediaType mediaType);
    
    //Count lessons in a topic
    long countByTopicId(Long topicId);
    
    //Count lessons in a course
    long countByCourseId(Long courseId);
}