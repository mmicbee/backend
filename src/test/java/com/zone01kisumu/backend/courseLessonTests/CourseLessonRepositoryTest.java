package com.zone01kisumu.backend.courseLessonTests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.repository.CourseLessonRepository;

class CourseLessonRepositoryTest {

    private CourseLessonRepository repository;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(CourseLessonRepository.class);
    }

    @Test
    void testFindByCourseId() {
        CourseLesson lesson1 = new CourseLesson();
        lesson1.setId(1L);
        lesson1.setCourseId(10L);

        when(repository.findByCourseId(10L)).thenReturn(List.of(lesson1));

        List<CourseLesson> result = repository.findByCourseId(10L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(repository).findByCourseId(10L);
    }

    @Test
    void testFindByTopicId() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(2L);
        lesson.setTopicId(5L);

        when(repository.findByTopicId(5L)).thenReturn(List.of(lesson));

        List<CourseLesson> result = repository.findByTopicId(5L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        verify(repository).findByTopicId(5L);
    }

    @Test
    void testFindByRecordedMediaType() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(3L);

        when(repository.findByRecordedMediaType(CourseLesson.RecordedMediaType.VIDEO))
                .thenReturn(List.of(lesson));

        List<CourseLesson> result = repository.findByRecordedMediaType(
                CourseLesson.RecordedMediaType.VIDEO
        );

        assertEquals(1, result.size());
        verify(repository).findByRecordedMediaType(CourseLesson.RecordedMediaType.VIDEO);
    }

    @Test
    void testCountByTopicId() {
        when(repository.countByTopicId(7L)).thenReturn(4L);

        long count = repository.countByTopicId(7L);

        assertEquals(4L, count);
        verify(repository).countByTopicId(7L);
    }

    @Test
    void testCountByCourseId() {
        when(repository.countByCourseId(3L)).thenReturn(9L);

        long count = repository.countByCourseId(3L);

        assertEquals(9L, count);
        verify(repository).countByCourseId(3L);
    }
}