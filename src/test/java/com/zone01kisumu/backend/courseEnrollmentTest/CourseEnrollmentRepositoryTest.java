package com.zone01kisumu.backend.courseEnrollmentTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.zone01kisumu.backend.model.CourseEnrollment;
import com.zone01kisumu.backend.model.CourseEnrollment.PaymentStatus;
import com.zone01kisumu.backend.repository.CourseEnrollmentRepository;

// Test class for CourseEnrollmentRepository
class CourseEnrollmentRepositoryTest {

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByStudentId() {
        Long studentId = 1L;
        List<CourseEnrollment> expected = Arrays.asList(new CourseEnrollment(), new CourseEnrollment());

        when(courseEnrollmentRepository.findByStudentId(studentId)).thenReturn(expected);

        List<CourseEnrollment> actual = courseEnrollmentRepository.findByStudentId(studentId);

        assertEquals(expected.size(), actual.size());
        verify(courseEnrollmentRepository).findByStudentId(studentId);
    }

    @Test
    void testFindByCourseId() {
        Long courseId = 100L;
        List<CourseEnrollment> expected = Arrays.asList(new CourseEnrollment());

        when(courseEnrollmentRepository.findByCourseId(courseId)).thenReturn(expected);

        List<CourseEnrollment> actual = courseEnrollmentRepository.findByCourseId(courseId);

        assertEquals(expected.size(), actual.size());
        verify(courseEnrollmentRepository).findByCourseId(courseId);
    }

    @Test
    void testFindByStudentIdAndCourseId() {
        Long studentId = 1L;
        Long courseId = 100L;
        CourseEnrollment enrollment = new CourseEnrollment();

        when(courseEnrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(enrollment));

        Optional<CourseEnrollment> result = courseEnrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);

        assertTrue(result.isPresent());
        verify(courseEnrollmentRepository).findByStudentIdAndCourseId(studentId, courseId);
    }

    @Test
    void testFindByStudentIdAndPaymentStatus() {
        Long studentId = 1L;
        PaymentStatus status = PaymentStatus.COMPLETED;
        List<CourseEnrollment> expected = Arrays.asList(new CourseEnrollment());

        when(courseEnrollmentRepository.findByStudentIdAndPaymentStatus(studentId, status)).thenReturn(expected);

        List<CourseEnrollment> actual = courseEnrollmentRepository.findByStudentIdAndPaymentStatus(studentId, status);

        assertEquals(expected.size(), actual.size());
        verify(courseEnrollmentRepository).findByStudentIdAndPaymentStatus(studentId, status);
    }

    @Test
    void testCountCompletedEnrollmentsByCourseId() {
        Long courseId = 100L;
        Long expectedCount = 5L;

        when(courseEnrollmentRepository.countCompletedEnrollmentsByCourseId(courseId)).thenReturn(expectedCount);

        Long actualCount = courseEnrollmentRepository.countCompletedEnrollmentsByCourseId(courseId);

        assertEquals(expectedCount, actualCount);
        verify(courseEnrollmentRepository).countCompletedEnrollmentsByCourseId(courseId);
    }

    @Test
    void testFindEnrollmentsByTeacherId() {
        Long teacherId = 10L;
        List<CourseEnrollment> expected = Arrays.asList(new CourseEnrollment());

        when(courseEnrollmentRepository.findEnrollmentsByTeacherId(teacherId)).thenReturn(expected);

        List<CourseEnrollment> actual = courseEnrollmentRepository.findEnrollmentsByTeacherId(teacherId);

        assertEquals(expected.size(), actual.size());
        verify(courseEnrollmentRepository).findEnrollmentsByTeacherId(teacherId);
    }
}
