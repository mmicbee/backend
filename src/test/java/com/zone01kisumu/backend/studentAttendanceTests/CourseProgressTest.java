package com.zone01kisumu.backend.studentAttendanceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.CourseProgress;
import com.zone01kisumu.backend.model.Student;

class CourseProgressTest {

    private Student student;
    private CourseTopic courseTopic;
    private Course course;

    @BeforeEach
    void setUP() {
        course = mock(Course.class);
        student = mock(Student.class);
        courseTopic = mock(CourseTopic.class);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime timeNow = LocalDateTime.now();
        CourseProgress progress = new CourseProgress(
                1L,
                student,
                courseTopic,
                course,
                12,
                BigDecimal.valueOf(50.0),
                CourseProgress.Status.IN_PROGRESS,
                timeNow);

        assertEquals(1L, progress.getId());
        assertEquals(student, progress.getStudent());
        assertEquals(courseTopic, progress.getTopic());
        assertEquals(course, progress.getCourse());
        assertEquals(12, progress.getCompletedLessonsCount());
        assertEquals(BigDecimal.valueOf(50.0), progress.getProgressPercentage());
        assertEquals(CourseProgress.Status.IN_PROGRESS, progress.getStatus());
        assertEquals(timeNow, progress.getLastUpdated());

    }

    @Test
    void testSettersAndGetters() {
        CourseProgress progress = new CourseProgress();

        progress.setId(2L);
        progress.setStudent(student);
        progress.setTopic(courseTopic);
        progress.setCourse(course);
        progress.setCompletedLessonsCount(12);
        progress.setProgressPercentage(BigDecimal.valueOf(50.0));
        progress.setStatus(CourseProgress.Status.IN_PROGRESS);
        LocalDateTime timeNow = LocalDateTime.now();
        progress.setLastUpdated(timeNow);

        assertEquals(2L, progress.getId());
        assertEquals(student, progress.getStudent());
        assertEquals(courseTopic, progress.getTopic());
        assertEquals(course, progress.getCourse());
        assertEquals(12, progress.getCompletedLessonsCount());
        assertEquals(CourseProgress.Status.IN_PROGRESS, progress.getStatus());
        assertEquals(BigDecimal.valueOf(50.0), progress.getProgressPercentage());
        assertEquals(timeNow, progress.getLastUpdated());

    }

    @Test
    void testEnumValues() {
        assertEquals("COMPLETED", CourseProgress.Status.COMPLETED.name());
        assertEquals("IN_PROGRESS", CourseProgress.Status.IN_PROGRESS.name());
        assertEquals("NOT_STARTED", CourseProgress.Status.NOT_STARTED.name());
    }

}
