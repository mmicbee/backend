package com.zone01kisumu.backend.studentAttendanceTests;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.StudentAttendanceDTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StudentAttendanceDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidStudentAttendanceDTO() {
        StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                .id(1L)
                .studentId(100L)
                .courseLessonId(200L)
                .attendanceStatus("PRESENT")
                .markedBy("TEACHER")
                .build();

        Set<ConstraintViolation<StudentAttendanceDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no validation violations");
    }

    @Test
    void testInvalidStudentId() {
        StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                .studentId(0L) // Invalid: must be >= 1
                .courseLessonId(100L)
                .attendanceStatus("ABSENT")
                .markedBy("SYSTEM")
                .build();

        Set<ConstraintViolation<StudentAttendanceDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("studentId")));
    }

    @Test
    void testBlankAttendanceStatusAndMarkedBy() {
        StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                .studentId(1L)
                .courseLessonId(2L)
                .attendanceStatus("  ") // Invalid: blank
                .markedBy("") // Invalid: blank
                .build();

        Set<ConstraintViolation<StudentAttendanceDTO>> violations = validator.validate(dto);
        assertEquals(2, violations.size());

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("attendanceStatus")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("markedBy")));
    }

    @Test
    void testNullValues() {
        StudentAttendanceDTO dto = new StudentAttendanceDTO();

        Set<ConstraintViolation<StudentAttendanceDTO>> violations = validator.validate(dto);

        assertEquals(4, violations.size()); // All except ID are invalid (nullable or blank)
    }

    @Test
    void testSettersAndGetters() {
        StudentAttendanceDTO dto = new StudentAttendanceDTO();
        dto.setId(10L);
        dto.setStudentId(101L);
        dto.setCourseLessonId(202L);
        dto.setAttendanceStatus("EXCUSED");
        dto.setMarkedBy("SYSTEM");

        assertEquals(10L, dto.getId());
        assertEquals(101L, dto.getStudentId());
        assertEquals(202L, dto.getCourseLessonId());
        assertEquals("EXCUSED", dto.getAttendanceStatus());
        assertEquals("SYSTEM", dto.getMarkedBy());
    }
}


