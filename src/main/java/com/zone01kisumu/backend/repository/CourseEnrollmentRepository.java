package com.zone01kisumu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zone01kisumu.backend.model.CourseEnrollment;

// Repository for Course Enrollment persistence operations
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    List<CourseEnrollment> findByStudentId(Long studentId);

    List<CourseEnrollment> findByCourseId(Long courseId);

    Optional<CourseEnrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.studentId = :studentId AND ce.paymentStatus = :status")
    List<CourseEnrollment> findByStudentIdAndPaymentStatus(@Param("studentId") Long studentId,
            @Param("status") CourseEnrollment.PaymentStatus status);

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.id = :courseId " +
            "AND ce.paymentStatus = 'COMPLETED'")
    Long countCompletedEnrollmentsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT ce FROM CourseEnrollment ce JOIN ce.course c WHERE c.teacherId = :teacherId")
    List<CourseEnrollment> findEnrollmentsByTeacherId(@Param("teacherId") Long teacherId);

}
