package com.zone01kisumu.backend.studentAttendanceTests;

import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.LiveLesson;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.StudentAttendance;
import com.zone01kisumu.backend.model.StudentAttendance.AttendanceStatus;
import com.zone01kisumu.backend.repository.StudentAttendanceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAttendanceRepositoryTest {

    private StudentAttendanceRepository repository;

    private Student student;
    private CourseLesson courseLesson;
    private CourseTopic topic;

    @BeforeEach
    void setUp() {
        repository = mock(StudentAttendanceRepository.class);
        student = new Student(); // These would ideally be test stubs or mock data
        courseLesson = new LiveLesson();
        topic = new CourseTopic();
    }

    @Test
    void testFindByStudentAndCourseLesson() {
        StudentAttendance mockAttendance = new StudentAttendance();
        when(repository.findByStudentAndCourseLesson(student, courseLesson))
                .thenReturn(Optional.of(mockAttendance));

        Optional<StudentAttendance> result = repository.findByStudentAndCourseLesson(student, courseLesson);

        assertTrue(result.isPresent());
        assertEquals(mockAttendance, result.get());
        verify(repository, times(1)).findByStudentAndCourseLesson(student, courseLesson);
    }

    @Test
    void testCountByStudentAndCourseLesson_TopicAndAttendanceStatus() {
        when(repository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                student, topic, AttendanceStatus.PRESENT)).thenReturn(5L);

        Long count = repository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                student, topic, AttendanceStatus.PRESENT);

        assertEquals(5L, count);
        verify(repository, times(1))
                .countByStudentAndCourseLesson_TopicAndAttendanceStatus(student, topic, AttendanceStatus.PRESENT);
    }
}
