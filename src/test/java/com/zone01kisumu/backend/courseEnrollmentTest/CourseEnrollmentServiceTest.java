package com.zone01kisumu.backend.courseEnrollmentTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;

import com.zone01kisumu.backend.dto.CourseEnrollmentDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseEnrollment;
import com.zone01kisumu.backend.model.CourseEnrollment.PaymentMethod;
import com.zone01kisumu.backend.model.CourseEnrollment.PaymentStatus;
import com.zone01kisumu.backend.repository.CourseEnrollmentRepository;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.service.CourseEnrollmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class CourseEnrollmentServiceTest {

    @InjectMocks
    private CourseEnrollmentService service;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ======= enrollStudent tests =======
    @Test
    void enrollStudent_nullStudentOrCourse_throws() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(null);
        dto.setCourseId(1L);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.enrollStudent(dto));
        assertTrue(ex.getMessage().contains("Student ID and Course ID must be provided"));
    }

    @Test
    void enrollStudent_alreadyEnrolled_throws() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(2L);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L))
                .thenReturn(Optional.of(new CourseEnrollment()));

        assertThrows(IllegalStateException.class, () -> service.enrollStudent(dto));
    }

    @Test
    void enrollStudent_courseNotFound_throws() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(2L);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.enrollStudent(dto));
    }

    @Test
    void enrollStudent_courseNotOpen_throws() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(2L);

        Course course = new Course();
        course.setId(2L);
        course.setStatus(Course.Status.CLOSED);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThrows(IllegalStateException.class, () -> service.enrollStudent(dto));
    }

    @Test
    void enrollStudent_freeRecordedCourse_savesEnrollment() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(2L);
        dto.setPaymentStatus("PENDING");
        dto.setPaymentMethod("CREDIT_CARD");

        Course course = new Course();
        course.setId(2L);
        course.setStatus(Course.Status.OPEN);
        course.setMode(Course.Mode.RECORDED);
        course.setPrice(BigDecimal.ZERO);
        course.setDuration(2);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(enrollmentRepository.save(any())).thenAnswer(i -> {
            CourseEnrollment e = i.getArgument(0);
            e.setId(10L);
            return e;
        });

        CourseEnrollmentDTO result = service.enrollStudent(dto);
        assertEquals(10L, result.getId());
        assertEquals("PENDING", result.getPaymentStatus());
        assertEquals(BigDecimal.ZERO, result.getAmountPaidNow());
    }

    @Test
    void enrollStudent_paidRecordedCourse_invalidPayment_throws() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(2L);
        dto.setAmountPaidNow(BigDecimal.ZERO);

        Course course = new Course();
        course.setId(2L);
        course.setStatus(Course.Status.OPEN);
        course.setMode(Course.Mode.RECORDED);
        course.setPrice(BigDecimal.valueOf(100));

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThrows(IllegalArgumentException.class, () -> service.enrollStudent(dto));
    }

    @Test
    void enrollStudent_paidRecordedCourse_fullPayment_saves() {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(2L);
        dto.setAmountPaidNow(BigDecimal.valueOf(100));
        dto.setPaymentMethod("CREDIT_CARD");

        Course course = new Course();
        course.setId(2L);
        course.setStatus(Course.Status.OPEN);
        course.setMode(Course.Mode.RECORDED);
        course.setPrice(BigDecimal.valueOf(100));
        course.setDuration(1);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(enrollmentRepository.save(any())).thenAnswer(i -> {
            CourseEnrollment e = i.getArgument(0);
            e.setId(11L);
            return e;
        });

        CourseEnrollmentDTO result = service.enrollStudent(dto);
        assertEquals("COMPLETED", result.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(100), result.getAmountPaidNow());
    }

    // ======= updatePaymentStatus tests =======
    @Test
    void updatePaymentStatus_overpayment_throws() {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setAmountPaid(BigDecimal.valueOf(50));
        enrollment.setAmountDue(BigDecimal.valueOf(100));

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L))
                .thenReturn(Optional.of(enrollment));

        assertThrows(IllegalArgumentException.class,
                () -> service.updatePaymentStatus(1L, 2L, BigDecimal.valueOf(60)));
    }

    @Test
    void updatePaymentStatus_partialPayment_updates() {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(new Course());
        enrollment.getCourse().setId(2L);
        enrollment.setAmountPaid(BigDecimal.valueOf(20));
        enrollment.setAmountDue(BigDecimal.valueOf(100));
        enrollment.setPaymentStatus(PaymentStatus.PENDING);
        enrollment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CourseEnrollmentDTO dto = service.updatePaymentStatus(1L, 2L, BigDecimal.valueOf(30));
        assertEquals(PaymentStatus.PARTIALLY_PAID.name(), dto.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(30), dto.getAmountPaidNow());
    }

    @Test
    void updatePaymentStatus_fullPayment_updates() {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(new Course());
        enrollment.getCourse().setId(2L);
        enrollment.setAmountPaid(BigDecimal.valueOf(20));
        enrollment.setAmountDue(BigDecimal.valueOf(50));
        enrollment.setPaymentStatus(PaymentStatus.PENDING);
        enrollment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CourseEnrollmentDTO dto = service.updatePaymentStatus(1L, 2L, BigDecimal.valueOf(30));
        assertEquals(PaymentStatus.COMPLETED.name(), dto.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(30), dto.getAmountPaidNow());
    }

    // ======= getter tests =======
    @Test
    void getEnrollmentsByStudent_returnsList() {
        CourseEnrollment e = new CourseEnrollment();
        e.setId(1L);
        e.setAmountPaid(BigDecimal.ZERO);
        e.setAmountDue(BigDecimal.ZERO);
        e.setPaymentStatus(PaymentStatus.PENDING);
        e.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Course c = new Course();
        c.setId(2L);
        e.setCourse(c);

        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(e));

        List<CourseEnrollmentDTO> list = service.getEnrollmentsByStudent(1L);
        assertEquals(1, list.size());
    }

    @Test
    void getEnrollmentsByCourse_returnsList() {
        CourseEnrollment e = new CourseEnrollment();
        e.setId(1L);
        e.setAmountPaid(BigDecimal.ZERO);
        e.setAmountDue(BigDecimal.ZERO);
        e.setPaymentStatus(PaymentStatus.PENDING);
        e.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Course c = new Course();
        c.setId(2L);
        e.setCourse(c);

        when(enrollmentRepository.findByCourseId(2L)).thenReturn(List.of(e));

        List<CourseEnrollmentDTO> list = service.getEnrollmentsByCourse(2L);
        assertEquals(1, list.size());
    }

    @Test
    void getEnrollmentsByTeacher_returnsList() {
        CourseEnrollment e = new CourseEnrollment();
        e.setId(1L);
        e.setAmountPaid(BigDecimal.ZERO);
        e.setAmountDue(BigDecimal.ZERO);
        e.setPaymentStatus(PaymentStatus.PENDING);
        e.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Course c = new Course();
        c.setId(2L);
        e.setCourse(c);

        when(enrollmentRepository.findEnrollmentsByTeacherId(5L)).thenReturn(List.of(e));

        List<CourseEnrollmentDTO> list = service.getEnrollmentsByTeacher(5L);
        assertEquals(1, list.size());
    }

    @Test
    void getEnrollment_presentAndEmpty() {
        CourseEnrollment e = new CourseEnrollment();
        e.setId(1L);
        e.setAmountPaid(BigDecimal.ZERO);
        e.setAmountDue(BigDecimal.ZERO);
        e.setPaymentStatus(PaymentStatus.PENDING);
        e.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Course c = new Course();
        c.setId(2L);
        e.setCourse(c);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 2L)).thenReturn(Optional.of(e));
        Optional<CourseEnrollmentDTO> dto = service.getEnrollment(1L, 2L);
        assertTrue(dto.isPresent());

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 3L)).thenReturn(Optional.empty());
        Optional<CourseEnrollmentDTO> dto2 = service.getEnrollment(1L, 3L);
        assertFalse(dto2.isPresent());
    }

    @Test
    void countCompletedEnrollments_callsRepository() {
        when(enrollmentRepository.countCompletedEnrollmentsByCourseId(2L)).thenReturn(5L);
        Long count = service.countCompletedEnrollments(2L);
        assertEquals(5L, count);
    }
}
