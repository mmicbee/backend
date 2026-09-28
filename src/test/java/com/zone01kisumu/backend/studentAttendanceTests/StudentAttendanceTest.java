package com.zone01kisumu.backend.studentAttendanceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.StudentAttendance;

class StudentAttendanceTest {
    private Student student;
    private CourseLesson courseLesson;

    @BeforeEach
    void setUp() {
        student = mock(Student.class);
        courseLesson = mock(CourseLesson.class);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        StudentAttendance attendance = new StudentAttendance(
                1L,
                student,
                courseLesson,
                StudentAttendance.AttendanceStatus.PRESENT,
                StudentAttendance.MarkedBy.TEACHER,
                now);

        assertEquals(1L, attendance.getId());
        assertEquals(student, attendance.getStudent());
        assertEquals(courseLesson, attendance.getCourseLesson());
        assertEquals(StudentAttendance.AttendanceStatus.PRESENT, attendance.getAttendanceStatus());
        assertEquals(StudentAttendance.MarkedBy.TEACHER, attendance.getMarkedBy());
        assertEquals(now, attendance.getMarkedAt());
    }

    @Test
    void testSettersAndGetters() {
        StudentAttendance attendance = new StudentAttendance();

        attendance.setId(2L);
        attendance.setStudent(student);
        attendance.setCourseLesson(courseLesson);
        attendance.setAttendanceStatus(StudentAttendance.AttendanceStatus.PRESENT);
        attendance.setMarkedBy(StudentAttendance.MarkedBy.TEACHER);
        LocalDateTime timestamp = LocalDateTime.now();
        attendance.setMarkedAt(timestamp);

        assertEquals(2L, attendance.getId());
        assertEquals(student, attendance.getStudent());
        assertEquals(courseLesson, attendance.getCourseLesson());
        assertEquals(StudentAttendance.AttendanceStatus.PRESENT, attendance.getAttendanceStatus());
        assertEquals(StudentAttendance.MarkedBy.TEACHER, attendance.getMarkedBy());
        assertEquals(timestamp, attendance.getMarkedAt());
    }

    @Test
    void testEnumValues() {
        assertEquals("PRESENT", StudentAttendance.AttendanceStatus.PRESENT.name());
        assertEquals("ABSENT", StudentAttendance.AttendanceStatus.ABSENT.name());
        assertEquals("EXCUSED", StudentAttendance.AttendanceStatus.EXCUSED.name());

        assertEquals("TEACHER", StudentAttendance.MarkedBy.TEACHER.name());
        assertEquals("SYSTEM", StudentAttendance.MarkedBy.SYSTEM.name());
    }
}

