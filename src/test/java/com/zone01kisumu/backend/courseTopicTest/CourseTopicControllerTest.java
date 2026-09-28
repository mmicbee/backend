package com.zone01kisumu.backend.courseTopicTest;

import com.zone01kisumu.backend.controller.CourseTopicController;
import com.zone01kisumu.backend.dto.CourseTopicDTO;
import com.zone01kisumu.backend.service.CourseTopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// Test class for CourseTopicController
class CourseTopicControllerTest {

    @Mock
    private CourseTopicService courseTopicService;

    @InjectMocks
    private CourseTopicController courseTopicController;

    private CourseTopicDTO sampleDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleDTO = new CourseTopicDTO();
        sampleDTO.setId(1L);
        sampleDTO.setTitle("Intro to Java");
        sampleDTO.setDescription("Java basics");
        sampleDTO.setTotalExpectedLessons(5);
        sampleDTO.setDuration(10);
    }

    @Test
    void createTopic_shouldReturnCreated() {
        when(courseTopicService.createTopic(eq(100L), any(CourseTopicDTO.class))).thenReturn(sampleDTO);

        ResponseEntity<CourseTopicDTO> response = courseTopicController.createTopic(100L, sampleDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleDTO);
    }

    @Test
    void getTopics_shouldReturnList() {
        when(courseTopicService.getTopicsByCourse(100L)).thenReturn(List.of(sampleDTO));

        ResponseEntity<List<CourseTopicDTO>> response = courseTopicController.getTopics(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(sampleDTO);
    }

    @Test
    void getTopicById_shouldReturnTopic_whenFound() {
        when(courseTopicService.getTopicById(1L)).thenReturn(Optional.of(sampleDTO));

        ResponseEntity<CourseTopicDTO> response = courseTopicController.getTopicById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleDTO);
    }

    @Test
    void getTopicById_shouldReturnNotFound_whenMissing() {
        when(courseTopicService.getTopicById(1L)).thenReturn(Optional.empty());

        ResponseEntity<CourseTopicDTO> response = courseTopicController.getTopicById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void updateTopic_shouldReturnUpdated_whenFound() {
        when(courseTopicService.updateTopic(eq(1L), any(CourseTopicDTO.class))).thenReturn(Optional.of(sampleDTO));

        ResponseEntity<CourseTopicDTO> response = courseTopicController.updateTopic(1L, sampleDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleDTO);
    }

    @Test
    void updateTopic_shouldReturnNotFound_whenMissing() {
        when(courseTopicService.updateTopic(eq(1L), any(CourseTopicDTO.class))).thenReturn(Optional.empty());

        ResponseEntity<CourseTopicDTO> response = courseTopicController.updateTopic(1L, sampleDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void deleteTopic_shouldReturnNoContent_whenDeleted() {
        when(courseTopicService.deleteTopic(1L)).thenReturn(true);

        ResponseEntity<Void> response = courseTopicController.deleteTopic(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteTopic_shouldReturnNotFound_whenNotFound() {
        when(courseTopicService.deleteTopic(1L)).thenReturn(false);

        ResponseEntity<Void> response = courseTopicController.deleteTopic(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTotalDuration_shouldReturnDuration() {
        when(courseTopicService.getTotalDurationByCourseId(100L)).thenReturn(120);

        ResponseEntity<Integer> response = courseTopicController.getTotalDuration(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(120);
    }
}
