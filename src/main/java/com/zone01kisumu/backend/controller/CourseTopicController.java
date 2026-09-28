package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.dto.CourseTopicDTO;
import com.zone01kisumu.backend.service.CourseTopicService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

// class CourseTopicController is used to handle course Topic related requests
@RestController
@RequestMapping("/api/courses/{courseId}/topics")
@RequiredArgsConstructor
public class CourseTopicController {

    
    private final CourseTopicService courseTopicService;

    // Create Topic
    @PostMapping
    public ResponseEntity<CourseTopicDTO> createTopic(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseTopicDTO dto) {
        return ResponseEntity.status(201).body(courseTopicService.createTopic(courseId, dto));
    }

    // Get all topics for course
    @GetMapping
    public ResponseEntity<List<CourseTopicDTO>> getTopics(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseTopicService.getTopicsByCourse(courseId));
    }

    // Get Topic by ID
    @GetMapping("/{topicId}")
    public ResponseEntity<CourseTopicDTO> getTopicById(@PathVariable Long topicId) {
        Optional<CourseTopicDTO> topic = courseTopicService.getTopicById(topicId);
        return topic.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Update Topic
    @PutMapping("/{topicId}")
    public ResponseEntity<CourseTopicDTO> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody CourseTopicDTO dto) {
        Optional<CourseTopicDTO> updated = courseTopicService.updateTopic(topicId, dto);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Delete Topic
    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long topicId) {
        boolean deleted = courseTopicService.deleteTopic(topicId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Get total duration
    @GetMapping("/duration")
    public ResponseEntity<Integer> getTotalDuration(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseTopicService.getTotalDurationByCourseId(courseId));
    }
}
