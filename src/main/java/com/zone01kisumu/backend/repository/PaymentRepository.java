package com.zone01kisumu.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.CourseEnrollment;
import com.zone01kisumu.backend.model.Payment;

// Repository for payment persistence operations
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaystackReference(String reference);

    Optional<Payment> findByStudentIdAndCourseIdAndStatus(
            Long studentId, Long courseId, CourseEnrollment.PaymentStatus status);
}
