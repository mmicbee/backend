package com.zone01kisumu.backend.studentAttendanceTests;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.CourseProgress;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.CourseProgressRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CourseProgressRepositoryTest {

    private CourseProgressRepository repository;

    private Student student;
    private CourseTopic topic;
    private Course course;

    @BeforeEach
    void setUp() {
        repository = mock(CourseProgressRepository.class);
        student = new Student();
        topic = new CourseTopic();
        course = new Course();
    }

    @Test
    void testFindByStudentAndtopic() {
        CourseProgress progress = new CourseProgress();
        when(repository.findByStudentAndTopic(student, topic))
                .thenReturn(Optional.of(progress));

        Optional<CourseProgress> result = repository.findByStudentAndTopic(student, topic);

        assertTrue(result.isPresent());
        assertEquals(progress, result.get());
        verify(repository, times(1)).findByStudentAndTopic(student, topic);
    }

    @Test
    void testExistsByStudentIdAndTopicId() {
        when(repository.existsByStudentIdAndTopicId(1L, 2L)).thenReturn(true);

        boolean exists = repository.existsByStudentIdAndTopicId(1L, 2L);

        assertTrue(exists);
        verify(repository, times(1)).existsByStudentIdAndTopicId(1L, 2L);
    }

    @Test
    void testFindProgressPercentagesByStudentAndCourse() {
        List<Double> expectedPercentages = Arrays.asList(75.0, 88.5, 100.0);
        when(repository.findProgressPercentagesByStudentAndCourse(student, course))
                .thenReturn(expectedPercentages);

        List<Double> result = repository.findProgressPercentagesByStudentAndCourse(student, course);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedPercentages, result);
        verify(repository, times(1)).findProgressPercentagesByStudentAndCourse(student, course);
    }
}


