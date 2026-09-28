package com.zone01kisumu.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.zone01kisumu.backend.dto.CourseTopicDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.CourseTopicRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Service class for course Topic-related operations
@Service
@RequiredArgsConstructor
public class CourseTopicService {

    private final CourseTopicRepository topicRepository;

    private final CourseRepository courseRepository;

    // Create Topic
    @Transactional
    public CourseTopicDTO createTopic(Long courseId, CourseTopicDTO dto) {
        
        // Validate course existence
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID: " + courseId));

        CourseTopic topic = new CourseTopic();
        topic.setCourse(course);
        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setTotalExpectedLessons(dto.getTotalExpectedLessons());
        topic.setDuration(0); // Default duration to 0
        return mapToDTO(topicRepository.save(topic));
    }

    // Get all Topics for course
    public List<CourseTopicDTO> getTopicsByCourse(Long courseId) {
        return topicRepository.findByCourseId(courseId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Get Topic by ID
    public Optional<CourseTopicDTO> getTopicById(Long id) {
        return topicRepository.findById(id).map(this::mapToDTO);
    }

    // Update Topic
    @Transactional
    public Optional<CourseTopicDTO> updateTopic(Long id, CourseTopicDTO dto) {
        return topicRepository.findById(id).map(topic -> {
            topic.setTitle(dto.getTitle());
            topic.setDescription(dto.getDescription());
            topic.setTotalExpectedLessons(dto.getTotalExpectedLessons());
            topic.setDuration(dto.getDuration());
            return mapToDTO(topicRepository.save(topic));
        });
    }

    // Delete Topic
    public boolean deleteTopic(Long id) {
        if (!topicRepository.existsById(id))
            return false;
        topicRepository.deleteById(id);
        return true;
    }

    // Get total duration of a course
    public Integer getTotalDurationByCourseId(Long courseId) {
        return topicRepository.getTotalDurationByCourseId(courseId);
    }


    private CourseTopicDTO mapToDTO(CourseTopic entity) {
        CourseTopicDTO dto = new CourseTopicDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setDuration(entity.getDuration());
        dto.setTotalExpectedLessons(entity.getTotalExpectedLessons());
        return dto;
    }
}
