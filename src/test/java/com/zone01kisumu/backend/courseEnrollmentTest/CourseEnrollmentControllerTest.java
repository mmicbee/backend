package com.zone01kisumu.backend.courseEnrollmentTest;

import com.zone01kisumu.backend.controller.CourseEnrollmentController;
import com.zone01kisumu.backend.dto.CourseEnrollmentDTO;
import com.zone01kisumu.backend.service.CourseEnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// Test class for CourseEnrollmentController
class CourseEnrollmentControllerTest {

    @Mock
    private CourseEnrollmentService enrollmentService;

    @InjectMocks
    private CourseEnrollmentController enrollmentController;

    private CourseEnrollmentDTO sampleEnrollment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleEnrollment = new CourseEnrollmentDTO();
        sampleEnrollment.setId(1L);
        sampleEnrollment.setStudentId(100L);
        sampleEnrollment.setCourseId(200L);
        sampleEnrollment.setEnrollmentDate(LocalDateTime.now());
        sampleEnrollment.setPaymentStatus("PENDING");
        sampleEnrollment.setPaymentMethod("MPESA");
        sampleEnrollment.setAmountPaidNow(BigDecimal.ZERO);
        sampleEnrollment.setAmountRemaining(new BigDecimal("1000.00"));
    }

    @Test
    void enrollStudent_shouldReturnCreatedEnrollment() {
        when(enrollmentService.enrollStudent(any(CourseEnrollmentDTO.class))).thenReturn(sampleEnrollment);

        ResponseEntity<CourseEnrollmentDTO> response = enrollmentController.enrollStudent(sampleEnrollment);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleEnrollment);
    }

    @Test
    void getEnrollmentsByStudent_shouldReturnList() {
        when(enrollmentService.getEnrollmentsByStudent(100L)).thenReturn(List.of(sampleEnrollment));

        ResponseEntity<List<CourseEnrollmentDTO>> response = enrollmentController.getEnrollmentsByStudent(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(sampleEnrollment);
    }

    @Test
    void getEnrollmentsByCourse_shouldReturnList() {
        when(enrollmentService.getEnrollmentsByCourse(200L)).thenReturn(List.of(sampleEnrollment));

        ResponseEntity<List<CourseEnrollmentDTO>> response = enrollmentController.getEnrollmentsByCourse(200L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(sampleEnrollment);
    }

    @Test
    void getEnrollment_shouldReturnEnrollment_whenFound() {
        when(enrollmentService.getEnrollment(100L, 200L)).thenReturn(Optional.of(sampleEnrollment));

        ResponseEntity<CourseEnrollmentDTO> response = enrollmentController.getEnrollment(100L, 200L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleEnrollment);
    }

    @Test
    void getEnrollment_shouldReturnNotFound_whenMissing() {
        when(enrollmentService.getEnrollment(100L, 200L)).thenReturn(Optional.empty());

        ResponseEntity<CourseEnrollmentDTO> response = enrollmentController.getEnrollment(100L, 200L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void countCompletedEnrollments_shouldReturnCount() {
        when(enrollmentService.countCompletedEnrollments(200L)).thenReturn(3L);

        ResponseEntity<Long> response = enrollmentController.countCompletedEnrollments(200L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(3L);
    }

    @Test
    void getEnrollmentsByTeacher_shouldReturnList() {
        when(enrollmentService.getEnrollmentsByTeacher(300L)).thenReturn(List.of(sampleEnrollment));

        ResponseEntity<List<CourseEnrollmentDTO>> response = enrollmentController.getEnrollmentsByTeacher(300L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(sampleEnrollment);
    }

    @Test
    void updatePayment_shouldReturnUpdatedEnrollment() {
        BigDecimal paid = new BigDecimal("500.00");
        CourseEnrollmentDTO paymentDTO = new CourseEnrollmentDTO();
        paymentDTO.setAmountPaidNow(paid);

        CourseEnrollmentDTO updatedEnrollment = new CourseEnrollmentDTO();
        updatedEnrollment.setId(1L);
        updatedEnrollment.setStudentId(100L);
        updatedEnrollment.setCourseId(200L);
        updatedEnrollment.setAmountPaidNow(paid);
        updatedEnrollment.setPaymentStatus("PARTIALLY_PAID");
        updatedEnrollment.setPaymentMethod("MPESA");
        updatedEnrollment.setAmountRemaining(new BigDecimal("500.00"));
        updatedEnrollment.setEnrollmentDate(LocalDateTime.now());

        when(enrollmentService.updatePaymentStatus(100L, 200L, paid)).thenReturn(updatedEnrollment);

        ResponseEntity<CourseEnrollmentDTO> response = enrollmentController.updatePayment(100L, 200L, paymentDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updatedEnrollment);
    }
}

