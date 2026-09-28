package com.zone01kisumu.backend.courseTopicTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.zone01kisumu.backend.dto.CourseTopicDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.CourseTopicRepository;
import com.zone01kisumu.backend.service.CourseTopicService;

class CourseTopicServiceTest {

    @Mock
    private CourseTopicRepository topicRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseTopicService courseTopicService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private CourseTopicDTO createTestDTO() {
        CourseTopicDTO dto = new CourseTopicDTO();
        dto.setTitle("Intro");
        dto.setDescription("Introduction");
        dto.setTotalExpectedLessons(5);
        dto.setDuration(10);
        return dto;
    }

    @Test
    void testCreateTopic_Success() {
        Long courseId = 1L;
        Course course = new Course();
        course.setId(courseId);
        CourseTopicDTO dto = createTestDTO();

        CourseTopic topicToSave = new CourseTopic();
        topicToSave.setCourse(course);
        topicToSave.setTitle(dto.getTitle());
        topicToSave.setDescription(dto.getDescription());
        topicToSave.setTotalExpectedLessons(dto.getTotalExpectedLessons());
        topicToSave.setDuration(0); // Service defaults this to 0

        CourseTopic savedTopic = new CourseTopic();
        savedTopic.setId(100L);
        savedTopic.setCourse(course);
        savedTopic.setTitle(dto.getTitle());
        savedTopic.setDuration(0);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(topicRepository.save(any(CourseTopic.class))).thenReturn(savedTopic);

        CourseTopicDTO result = courseTopicService.createTopic(courseId, dto);

        assertNotNull(result);
        assertEquals("Intro", result.getTitle());
        assertEquals(0, result.getDuration()); // Verify service logic
        verify(courseRepository).findById(courseId);
        verify(topicRepository).save(any(CourseTopic.class));
    }

    @Test
    void testGetTopicsByCourse() {
        CourseTopic topic = new CourseTopic();
        topic.setId(1L);
        topic.setTitle("Lesson 1");
        topic.setTotalExpectedLessons(1);
        topic.setDuration(5);

        when(topicRepository.findByCourseId(1L)).thenReturn(List.of(topic));

        List<CourseTopicDTO> result = courseTopicService.getTopicsByCourse(1L);

        assertEquals(1, result.size());
        assertEquals("Lesson 1", result.get(0).getTitle());
    }

    @Test
    void testUpdateTopic_Success() {
        CourseTopic existing = new CourseTopic();
        existing.setId(1L);
        existing.setTitle("Old Title");

        CourseTopicDTO dto = createTestDTO();
        dto.setTitle("New Title");

        when(topicRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(topicRepository.save(any(CourseTopic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<CourseTopicDTO> result = courseTopicService.updateTopic(1L, dto);

        assertTrue(result.isPresent());
        assertEquals("New Title", result.get().getTitle());
        assertEquals(dto.getDescription(), result.get().getDescription());
        verify(topicRepository).save(any(CourseTopic.class));
    }

    @Test
    void testDeleteTopic_Success() {
        when(topicRepository.existsById(1L)).thenReturn(true);
        doNothing().when(topicRepository).deleteById(1L);

        boolean result = courseTopicService.deleteTopic(1L);

        assertTrue(result);
        verify(topicRepository).deleteById(1L);
    }
}
