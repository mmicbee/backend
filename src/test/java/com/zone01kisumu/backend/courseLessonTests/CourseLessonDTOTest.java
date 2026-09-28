package com.zone01kisumu.backend.courseLessonTests;

import com.zone01kisumu.backend.dto.CourseLessonDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseLessonDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CourseLessonDTO.CourseLessonDTOBuilder getValidCourseLessonDTOBuilder() {
        return CourseLessonDTO.builder()
                .courseId(2L)
                .topicId(3L)
                .lessonName("Lesson 1");
    }

    @Test
    void whenAllFieldsAreValid_thenNoViolations() {
        CourseLessonDTO dto = getValidCourseLessonDTOBuilder().build();
        Set<ConstraintViolation<CourseLessonDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no constraint violations");
    }

    @Test
    void whenCourseIdIsNull_thenViolationOccurs() {
        CourseLessonDTO dto = getValidCourseLessonDTOBuilder().courseId(null).build();
        Set<ConstraintViolation<CourseLessonDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("courseId")));
    }

    @Test
    void whenCourseIdIsZero_thenViolationOccurs() {
        CourseLessonDTO dto = getValidCourseLessonDTOBuilder().courseId(0L).build();
        Set<ConstraintViolation<CourseLessonDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("courseId")));
    }

    @Test
    void whenLessonNameIsBlank_thenViolationOccurs() {
        CourseLessonDTO dto = getValidCourseLessonDTOBuilder().lessonName(" ").build();
        Set<ConstraintViolation<CourseLessonDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v
        .getPropertyPath().toString().equals("lessonName")));
    }
}