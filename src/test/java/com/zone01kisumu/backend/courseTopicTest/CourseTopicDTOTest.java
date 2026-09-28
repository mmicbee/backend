package com.zone01kisumu.backend.courseTopicTest;

import com.zone01kisumu.backend.dto.CourseTopicDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CourseTopicDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CourseTopicDTO createValidDTO() {
        CourseTopicDTO dto = new CourseTopicDTO();
        dto.setId(1L);
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");
        dto.setTotalExpectedLessons(5);
        dto.setDuration(60);
        return dto;
    }

    @Test
    void whenValidCourseTopicDTO_thenNoViolations() {
        CourseTopicDTO dto = createValidDTO();
        Set<ConstraintViolation<CourseTopicDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenTitleIsBlank_thenViolation() {
        CourseTopicDTO dto = createValidDTO();
        dto.setTitle("");
        Set<ConstraintViolation<CourseTopicDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Topic title is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenTitleIsTooLong_thenViolation() {
        CourseTopicDTO dto = createValidDTO();
        dto.setTitle("a".repeat(256));
        Set<ConstraintViolation<CourseTopicDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Title must not exceed 255 characters", violations.iterator().next().getMessage());
    }

    @Test
    void whenDescriptionIsTooLong_thenViolation() {
        CourseTopicDTO dto = createValidDTO();
        dto.setDescription("a".repeat(513));
        Set<ConstraintViolation<CourseTopicDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Description must not exceed 512 characters", violations.iterator().next().getMessage());
    }

    @Test
    void whenDurationIsNull_thenViolation() {
        CourseTopicDTO dto = createValidDTO();
        dto.setDuration(null);
        Set<ConstraintViolation<CourseTopicDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Duration is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenDurationIsLessThanOne_thenViolation() {
        CourseTopicDTO dto = createValidDTO();
        dto.setDuration(0);
        Set<ConstraintViolation<CourseTopicDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Duration must be at least 1 minute", violations.iterator().next().getMessage());
    }

    @Test
    void whenTotalExpectedLessonsIsLessThanOne_thenViolation() {
        CourseTopicDTO dto = createValidDTO();
        dto.setTotalExpectedLessons(0);
        Set<ConstraintViolation<CourseTopicDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Total expected lessons must be at least 1", violations.iterator().next().getMessage());
    }
}
