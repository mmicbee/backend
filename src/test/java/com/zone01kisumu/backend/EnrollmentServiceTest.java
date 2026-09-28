package com.zone01kisumu.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zone01kisumu.backend.dto.CourseEnrollmentDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseEnrollment;
import com.zone01kisumu.backend.repository.CourseEnrollmentRepository;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.service.CourseEnrollmentService;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseEnrollmentService enrollmentService;

    private Course freeCourse;
    private Course paidCourse;

    @BeforeEach
    void setUp() {
        freeCourse = new Course();
        freeCourse.setId(1L);
        freeCourse.setStatus(Course.Status.OPEN);
        freeCourse.setMode(Course.Mode.RECORDED);
        freeCourse.setPrice(BigDecimal.ZERO);
        freeCourse.setDuration(4);

        paidCourse = new Course();
        paidCourse.setId(2L);
        paidCourse.setStatus(Course.Status.OPEN);
        paidCourse.setMode(Course.Mode.RECORDED);
        paidCourse.setPrice(BigDecimal.valueOf(100));
        paidCourse.setDuration(4);
    }

    @Test
    void testEnrollStudent() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(10L);
        dto.setCourseId(1L);

        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 1L)).thenReturn(Optional.empty());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(freeCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(freeCourse);
        when(enrollmentRepository.save(any(CourseEnrollment.class))).thenAnswer(invocation -> {
            CourseEnrollment saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        CourseEnrollmentDTO result = enrollmentService.enrollStudent(dto);
        assertNotNull(result, "Student enrolled successfully");
        assertEquals(100L, result.getId());
        verify(enrollmentRepository).save(any(CourseEnrollment.class));
    }

    @Test
    void enrollFreeCourse_SuccessWithoutPaymentInfo() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(10L);
        dto.setCourseId(1L);

        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 1L)).thenReturn(Optional.empty());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(freeCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(freeCourse);
        when(enrollmentRepository.save(any(CourseEnrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseEnrollmentDTO result = enrollmentService.enrollStudent(dto);
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getAmountPaidNow());
        verify(enrollmentRepository).save(any(CourseEnrollment.class));
    }

    @Test
    void enrollPaidCourse_FailsWithoutPaymentConfirmation() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(10L);
        dto.setCourseId(2L);
        dto.setAmountPaidNow(null);

        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(2L)).thenReturn(Optional.of(paidCourse));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            enrollmentService.enrollStudent(dto);
        });

        assertTrue(ex.getMessage().contains("You must make a payment to enroll in this course"));
        verify(enrollmentRepository, never()).save(any(CourseEnrollment.class));
    }
}
