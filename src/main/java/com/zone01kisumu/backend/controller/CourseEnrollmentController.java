package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.dto.CourseEnrollmentDTO;
import com.zone01kisumu.backend.service.CourseEnrollmentService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

// class CourseEnrollmentController is used to handle course enrollment related requests
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    // Enroll a student
    @PostMapping
    public ResponseEntity<CourseEnrollmentDTO> enrollStudent(@Valid @RequestBody CourseEnrollmentDTO dto) {
        CourseEnrollmentDTO enrolled = enrollmentService.enrollStudent(dto);
        return ResponseEntity.status(201).body(enrolled);
    }

    // Get all enrollments by student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseEnrollmentDTO>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    // Get all enrollments by course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseEnrollmentDTO>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    // Get enrollment by student and course
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<CourseEnrollmentDTO> getEnrollment(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        Optional<CourseEnrollmentDTO> result = enrollmentService.getEnrollment(studentId, courseId);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Count completed enrollments for a course
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countCompletedEnrollments(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.countCompletedEnrollments(courseId));
    }

    // Get enrollments by teacher ID
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseEnrollmentDTO>> getEnrollmentsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByTeacher(teacherId));
    }

    // update payment status
    @PutMapping("/student/{studentId}/course/{courseId}/payment")
    public ResponseEntity<CourseEnrollmentDTO> updatePayment(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestBody CourseEnrollmentDTO paymentDTO) {
        CourseEnrollmentDTO updated = enrollmentService.updatePaymentStatus(studentId, courseId,
                paymentDTO.getAmountPaidNow());
        return ResponseEntity.ok(updated);
    }

}
