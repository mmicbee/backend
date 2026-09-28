package com.zone01kisumu.backend.courseTopicTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.repository.CourseTopicRepository;

//Test class for CourseTopicRepository
class CourseTopicRepositoryTest {

    @Mock
    private CourseTopicRepository courseTopicRepository;

    private Course testCourse;
    private CourseTopic testCourseTopic1;
    private CourseTopic testCourseTopic2;
    private CourseTopic testCourseTopic3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test course
        testCourse = new Course(1L);

        // Create test course topics
        testCourseTopic1 = new CourseTopic();
        testCourseTopic1.setId(1L);
        testCourseTopic1.setCourse(testCourse);
        testCourseTopic1.setTitle("Introduction to Java");
        testCourseTopic1.setDescription("Basic Java concepts");
        testCourseTopic1.setTotalExpectedLessons(5);
        testCourseTopic1.setDuration(60);

        testCourseTopic2 = new CourseTopic();
        testCourseTopic2.setId(2L);
        testCourseTopic2.setCourse(testCourse);
        testCourseTopic2.setTitle("Object-Oriented Programming");
        testCourseTopic2.setDescription("OOP concepts in Java");
        testCourseTopic2.setTotalExpectedLessons(10);
        testCourseTopic2.setDuration(90);

        testCourseTopic3 = new CourseTopic();
        testCourseTopic3.setId(3L);
        testCourseTopic3.setCourse(testCourse);
        testCourseTopic3.setTitle("Advanced Java");
        testCourseTopic3.setDescription("Advanced topics");
        testCourseTopic3.setTotalExpectedLessons(5);
        testCourseTopic3.setDuration(120);
    }

    @Test
    void testCountByCourse() {
        // Given
        long expectedCount = 3L;
        when(courseTopicRepository.countByCourse(testCourse)).thenReturn(expectedCount);

        // When
        long actualCount = courseTopicRepository.countByCourse(testCourse);

        // Then
        assertEquals(expectedCount, actualCount);
        verify(courseTopicRepository).countByCourse(testCourse);
    }

    @Test
    void testCountByCourse_WithNoCourseTopics() {
        // Given
        Course emptyCourse = new Course(2L);
        when(courseTopicRepository.countByCourse(emptyCourse)).thenReturn(0L);

        // When
        long actualCount = courseTopicRepository.countByCourse(emptyCourse);

        // Then
        assertEquals(0L, actualCount);
        verify(courseTopicRepository).countByCourse(emptyCourse);
    }

    @Test
    void testFindByCourseId() {
        // Given
        Long courseId = 1L;
        List<CourseTopic> expected = Arrays.asList(testCourseTopic1, testCourseTopic2, testCourseTopic3);
        when(courseTopicRepository.findByCourseId(courseId)).thenReturn(expected);

        // When
        List<CourseTopic> actual = courseTopicRepository.findByCourseId(courseId);

        // Then
        assertEquals(expected.size(), actual.size());
        assertEquals(3, actual.size());
        verify(courseTopicRepository).findByCourseId(courseId);
    }

    @Test
    void testFindByCourseId_WithNonExistentCourseId() {
        // Given
        Long courseId = 999L;
        when(courseTopicRepository.findByCourseId(courseId)).thenReturn(Collections.emptyList());

        // When
        List<CourseTopic> actual = courseTopicRepository.findByCourseId(courseId);

        // Then
        assertTrue(actual.isEmpty());
        verify(courseTopicRepository).findByCourseId(courseId);
    }

    @Test
    void testFindByCourseIdAndTotalExpectedLessons() {
        // Given
        Long courseId = 1L;
        Integer totalExpectedLessons = 5;
        List<CourseTopic> expected = Arrays.asList(testCourseTopic1, testCourseTopic3);
        when(courseTopicRepository.findByCourseIdAndTotalExpectedLessons(courseId, totalExpectedLessons))
                .thenReturn(expected);

        // When
        List<CourseTopic> actual = courseTopicRepository.findByCourseIdAndTotalExpectedLessons(
                courseId, totalExpectedLessons);

        // Then
        assertEquals(expected.size(), actual.size());
        assertEquals(2, actual.size());
        verify(courseTopicRepository).findByCourseIdAndTotalExpectedLessons(courseId, totalExpectedLessons);
    }

    @Test
    void testFindByCourseIdAndTotalExpectedLessons_WithNoMatches() {
        // Given
        Long courseId = 1L;
        Integer totalExpectedLessons = 100;
        when(courseTopicRepository.findByCourseIdAndTotalExpectedLessons(courseId, totalExpectedLessons))
                .thenReturn(Collections.emptyList());

        // When
        List<CourseTopic> actual = courseTopicRepository.findByCourseIdAndTotalExpectedLessons(
                courseId, totalExpectedLessons);

        // Then
        assertTrue(actual.isEmpty());
        verify(courseTopicRepository).findByCourseIdAndTotalExpectedLessons(courseId, totalExpectedLessons);
    }

    @Test
    void testFindByCourseIdAndTotalExpectedLessons_WithNonExistentCourseId() {
        // Given
        Long courseId = 999L;
        Integer totalExpectedLessons = 5;
        when(courseTopicRepository.findByCourseIdAndTotalExpectedLessons(courseId, totalExpectedLessons))
                .thenReturn(Collections.emptyList());

        // When
        List<CourseTopic> actual = courseTopicRepository.findByCourseIdAndTotalExpectedLessons(
                courseId, totalExpectedLessons);

        // Then
        assertTrue(actual.isEmpty());
        verify(courseTopicRepository).findByCourseIdAndTotalExpectedLessons(courseId, totalExpectedLessons);
    }

    @Test
    void testGetTotalDurationByCourseId() {
        // Given
        Long courseId = 1L;
        Integer expectedDuration = 270; // 60 + 90 + 120
        when(courseTopicRepository.getTotalDurationByCourseId(courseId)).thenReturn(expectedDuration);

        // When
        Integer actualDuration = courseTopicRepository.getTotalDurationByCourseId(courseId);

        // Then
        assertEquals(expectedDuration, actualDuration);
        verify(courseTopicRepository).getTotalDurationByCourseId(courseId);
    }

    @Test
    void testGetTotalDurationByCourseId_WithNoCourseTopics() {
        // Given
        Long courseId = 2L;
        when(courseTopicRepository.getTotalDurationByCourseId(courseId)).thenReturn(null);

        // When
        Integer actualDuration = courseTopicRepository.getTotalDurationByCourseId(courseId);

        // Then
        assertNull(actualDuration);
        verify(courseTopicRepository).getTotalDurationByCourseId(courseId);
    }

    @Test
    void testGetTotalDurationByCourseId_WithNonExistentCourseId() {
        // Given
        Long courseId = 999L;
        when(courseTopicRepository.getTotalDurationByCourseId(courseId)).thenReturn(null);

        // When
        Integer actualDuration = courseTopicRepository.getTotalDurationByCourseId(courseId);

        // Then
        assertNull(actualDuration);
        verify(courseTopicRepository).getTotalDurationByCourseId(courseId);
    }

    @Test
    void testGetTotalDurationByCourseId_WithZeroDurations() {
        // Given
        Long courseId = 3L;
        when(courseTopicRepository.getTotalDurationByCourseId(courseId)).thenReturn(0);

        // When
        Integer actualDuration = courseTopicRepository.getTotalDurationByCourseId(courseId);

        // Then
        assertEquals(0, actualDuration);
        verify(courseTopicRepository).getTotalDurationByCourseId(courseId);
    }

    // Test JpaRepository inherited methods

    @Test
    void testSave() {
        // Given
        when(courseTopicRepository.save(testCourseTopic1)).thenReturn(testCourseTopic1);

        // When
        CourseTopic saved = courseTopicRepository.save(testCourseTopic1);

        // Then
        assertNotNull(saved);
        assertEquals(testCourseTopic1.getId(), saved.getId());
        verify(courseTopicRepository).save(testCourseTopic1);
    }

    @Test
    void testFindById() {
        // Given
        Long id = 1L;
        when(courseTopicRepository.findById(id)).thenReturn(Optional.of(testCourseTopic1));

        // When
        Optional<CourseTopic> found = courseTopicRepository.findById(id);

        // Then
        assertTrue(found.isPresent());
        assertEquals(testCourseTopic1.getId(), found.get().getId());
        verify(courseTopicRepository).findById(id);
    }

    @Test
    void testFindById_WithNonExistentId() {
        // Given
        Long id = 999L;
        when(courseTopicRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<CourseTopic> found = courseTopicRepository.findById(id);

        // Then
        assertFalse(found.isPresent());
        verify(courseTopicRepository).findById(id);
    }

    @Test
    void testFindAll() {
        // Given
        List<CourseTopic> expected = Arrays.asList(testCourseTopic1, testCourseTopic2, testCourseTopic3);
        when(courseTopicRepository.findAll()).thenReturn(expected);

        // When
        List<CourseTopic> actual = courseTopicRepository.findAll();

        // Then
        assertEquals(expected.size(), actual.size());
        assertEquals(3, actual.size());
        verify(courseTopicRepository).findAll();
    }

    @Test
    void testCount() {
        // Given
        long expectedCount = 3L;
        when(courseTopicRepository.count()).thenReturn(expectedCount);

        // When
        long actualCount = courseTopicRepository.count();

        // Then
        assertEquals(expectedCount, actualCount);
        verify(courseTopicRepository).count();
    }

    @Test
    void testExistsById() {
        // Given
        Long id = 1L;
        when(courseTopicRepository.existsById(id)).thenReturn(true);

        // When
        boolean exists = courseTopicRepository.existsById(id);

        // Then
        assertTrue(exists);
        verify(courseTopicRepository).existsById(id);
    }

    @Test
    void testExistsById_WithNonExistentId() {
        // Given
        Long id = 999L;
        when(courseTopicRepository.existsById(id)).thenReturn(false);

        // When
        boolean exists = courseTopicRepository.existsById(id);

        // Then
        assertFalse(exists);
        verify(courseTopicRepository).existsById(id);
    }

    @Test
    void testDeleteById() {
        // Given
        Long id = 1L;
        doNothing().when(courseTopicRepository).deleteById(id);

        // When
        courseTopicRepository.deleteById(id);

        // Then
        verify(courseTopicRepository).deleteById(id);
    }

    @Test
    void testDelete() {
        // Given
        doNothing().when(courseTopicRepository).delete(testCourseTopic1);

        // When
        courseTopicRepository.delete(testCourseTopic1);

        // Then
        verify(courseTopicRepository).delete(testCourseTopic1);
    }

    @Test
    void testDeleteAll() {
        // Given
        doNothing().when(courseTopicRepository).deleteAll();

        // When
        courseTopicRepository.deleteAll();

        // Then
        verify(courseTopicRepository).deleteAll();
    }

    @Test
    void testSaveAll() {
        // Given
        List<CourseTopic> topics = Arrays.asList(testCourseTopic1, testCourseTopic2);
        when(courseTopicRepository.saveAll(topics)).thenReturn(topics);

        // When
        List<CourseTopic> saved = (List<CourseTopic>) courseTopicRepository.saveAll(topics);

        // Then
        assertEquals(topics.size(), saved.size());
        verify(courseTopicRepository).saveAll(topics);
    }
}
