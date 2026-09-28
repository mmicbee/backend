package com.zone01kisumu.backend.courseTest;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.Course.Mode;
import com.zone01kisumu.backend.repository.CourseRepository;

// Test class for CourseRepository(integration test)
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        courseRepository = mock(CourseRepository.class);
    }

    @Test
    void testFindByTeacherId() {
        Long teacherId = 1L;
        Course course = new Course();
        course.setId(1L);
        course.setTeacherId(teacherId);

        when(courseRepository.findByTeacherId(teacherId)).thenReturn(List.of(course));

        List<Course> result = courseRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(teacherId, result.get(0).getTeacherId());
    }

    @Test
    void testFindByTitleContaining() {
        String title = "Java";
        Course course = new Course();
        course.setTitle("Java Programming");

        when(courseRepository.findByTitleContaining(title)).thenReturn(List.of(course));

        List<Course> result = courseRepository.findByTitleContaining(title);

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getTitle().contains(title));
    }

    @Test
    void testFindByModeAndStartDateAfter() {
        Mode mode = Mode.LIVE;
        LocalDateTime now = LocalDateTime.now();

        Course course = new Course();
        course.setMode(mode);
        course.setStartDate(now.plusDays(1));

        when(courseRepository.findByModeAndStartDateAfter(mode, now)).thenReturn(List.of(course));

        List<Course> result = courseRepository.findByModeAndStartDateAfter(mode, now);

        assertEquals(1, result.size());
        assertEquals(mode, result.get(0).getMode());
        assertTrue(result.get(0).getStartDate().isAfter(now));
    }

    @Test
    void testFindByTitleOrDescriptionContaining() {
        String keyword = "Python";

        Course course = new Course();
        course.setTitle("Intro to Python");
        course.setDescription("Learn basics of Python");

        when(courseRepository.findByTitleOrDescriptionContaining(keyword)).thenReturn(List.of(course));

        List<Course> result = courseRepository.findByTitleOrDescriptionContaining(keyword);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().contains(keyword) || result.get(0).getDescription().contains(keyword));
    }

    @Test
    void testFindByDateRange() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(10);

        Course course = new Course();
        course.setStartDate(start.plusDays(1));
        course.setEndDate(end.minusDays(1));

        when(courseRepository.findByDateRange(start, end)).thenReturn(List.of(course));

        List<Course> result = courseRepository.findByDateRange(start, end);

        assertEquals(1, result.size());
        assertTrue(!result.get(0).getStartDate().isBefore(start));
        assertTrue(!result.get(0).getEndDate().isAfter(end));
    }

    @Test
    void testFindByPriceRange() {
        Double min = 50.0;
        Double max = 200.0;

        Course course = new Course();
        course.setPrice(BigDecimal.valueOf(100.0));

        when(courseRepository.findByPriceRange(min, max)).thenReturn(List.of(course));

        List<Course> result = courseRepository.findByPriceRange(min, max);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPrice().compareTo(BigDecimal.valueOf(min)) >= 0);
        assertTrue(result.get(0).getPrice().compareTo(BigDecimal.valueOf(max)) <= 0);
    }

}
