package com.zone01kisumu.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.zone01kisumu.backend.dto.CourseEnrollmentDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseEnrollment;
import com.zone01kisumu.backend.model.CourseEnrollment.PaymentStatus;
import com.zone01kisumu.backend.repository.CourseEnrollmentRepository;
import com.zone01kisumu.backend.repository.CourseRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Service class for course enrollment-related operations
@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;

    private final CourseRepository courseRepository;

    @Transactional
    public CourseEnrollmentDTO enrollStudent(CourseEnrollmentDTO dto) {
        if (dto.getStudentId() == null || dto.getCourseId() == null) {
            throw new IllegalArgumentException("Student ID and Course ID must be provided.");
        }

        // Prevent duplicate enrollment
        enrollmentRepository.findByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId())
                .ifPresent(e -> {
                    throw new IllegalStateException("Student is already enrolled in this course.");
                });

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + dto.getCourseId()));

        // only register if the course is open
        if (course.getStatus() != Course.Status.OPEN) {
            throw new IllegalStateException("Cannot enroll in a course that is not open for enrollment.");
        }
        // Determine whether course is free
        boolean isFree = (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0);
        BigDecimal amountPaid = (dto.getAmountPaidNow() != null) ? dto.getAmountPaidNow() : BigDecimal.ZERO;
        BigDecimal amountDue = (course.getPrice() != null) ? course.getPrice() : BigDecimal.ZERO;
        boolean isPendingPayment = "PENDING".equalsIgnoreCase(dto.getPaymentStatus());

        LocalDateTime enrollmentDate = LocalDateTime.now();
        if (course.getMode() == Course.Mode.LIVE && course.getStartDate() != null) {
            enrollmentDate = course.getStartDate();
        } else if (dto.getEnrollmentDate() != null) {
            enrollmentDate = dto.getEnrollmentDate();
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudentId(dto.getStudentId());
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(enrollmentDate);
        enrollment.setPaymentMethod(parsePaymentMethod(dto.getPaymentMethod()));
        enrollment.setAmountDue(amountDue);

        if (isFree) {
            PaymentStatus status = (dto.getPaymentStatus() != null && !dto.getPaymentStatus().isBlank())
                    ? parsePaymentStatus(dto.getPaymentStatus())
                    : PaymentStatus.COMPLETED;
            enrollment.setPaymentStatus(status);
            enrollment.setAmountPaid(BigDecimal.ZERO);
        } else if (isPendingPayment || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            if (!isPendingPayment && (dto.getAmountPaidNow() == null || dto.getAmountPaidNow().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new IllegalArgumentException("You must make a payment to enroll in this course.");
            }
            enrollment.setPaymentStatus(PaymentStatus.PENDING);
            enrollment.setAmountPaid(amountPaid);
        } else {
            if (amountPaid.compareTo(course.getPrice()) != 0) {
                throw new IllegalArgumentException("Payment must equal the course price for live courses.");
            }
            PaymentStatus status = (amountPaid.compareTo(amountDue) == 0) ? PaymentStatus.COMPLETED
                    : PaymentStatus.PARTIALLY_PAID;
            enrollment.setPaymentStatus(status);
            enrollment.setAmountPaid(amountPaid);
        }

        // update course start and end dates safely if needed
        if (course.getStartDate() == null) {
            course.setStartDate(enrollmentDate);
        }
        int durationWeeks = (course.getDuration() != null && course.getDuration() > 0) ? course.getDuration() : 4;
        if (course.getEndDate() == null) {
            course.setEndDate(enrollmentDate.plusWeeks(durationWeeks));
        }
        courseRepository.save(course);

        CourseEnrollment saved = enrollmentRepository.save(enrollment);
        return mapToDTO(saved);

    }

    public List<CourseEnrollmentDTO> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CourseEnrollmentDTO> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<CourseEnrollmentDTO> getEnrollment(Long studentId, Long courseId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .map(this::mapToDTO);
    }

    public Long countCompletedEnrollments(Long courseId) {
        return enrollmentRepository.countCompletedEnrollmentsByCourseId(courseId);
    }

    public List<CourseEnrollmentDTO> getEnrollmentsByTeacher(Long teacherId) {
        return enrollmentRepository.findEnrollmentsByTeacherId(teacherId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // ========== Mapping Methods ==========

    private CourseEnrollmentDTO mapToDTO(CourseEnrollment entity) {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudentId());
        dto.setCourseId(entity.getCourse().getId());
        dto.setEnrollmentDate(entity.getEnrollmentDate());
        dto.setPaymentStatus(entity.getPaymentStatus().name());
        dto.setPaymentMethod(entity.getPaymentMethod().name());
        dto.setAmountPaidNow(entity.getAmountPaid());
        dto.setAmountRemaining(entity.getAmountDue().subtract(entity.getAmountPaid()));
        return dto;
    }

    private CourseEnrollment.PaymentStatus parsePaymentStatus(String status) {
        try {
            return CourseEnrollment.PaymentStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return CourseEnrollment.PaymentStatus.PENDING;
        }
    }

    private CourseEnrollment.PaymentMethod parsePaymentMethod(String method) {
        try {
            return CourseEnrollment.PaymentMethod.valueOf(method.toUpperCase());
        } catch (Exception e) {
            return CourseEnrollment.PaymentMethod.CREDIT_CARD;
        }
    }

    // update payment status
    @Transactional
    public CourseEnrollmentDTO updatePaymentStatus(Long studentId, Long courseId, BigDecimal amountPaidNow) {
        if (amountPaidNow == null || amountPaidNow.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment amount.");
        }

        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .map(enrollment -> {
                    BigDecimal currentPaid = Optional.ofNullable(enrollment.getAmountPaid()).orElse(BigDecimal.ZERO);
                    BigDecimal amountDue = Optional.ofNullable(enrollment.getAmountDue()).orElse(BigDecimal.ZERO);
                    BigDecimal remaining = amountDue.subtract(currentPaid);

                    // Check for overpayment
                    if (amountPaidNow.compareTo(remaining) > 0) {
                        throw new IllegalArgumentException(
                                "Payment exceeds remaining balance. You can only pay up to " + remaining);
                    }

                    // Safe to proceed
                    BigDecimal newTotalPaid = currentPaid.add(amountPaidNow);
                    enrollment.setAmountPaid(newTotalPaid);

                    if (newTotalPaid.compareTo(amountDue) >= 0) {
                        enrollment.setPaymentStatus(PaymentStatus.COMPLETED);
                    } else {
                        enrollment.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
                    }

                    CourseEnrollment updated = enrollmentRepository.save(enrollment);
                    CourseEnrollmentDTO dto = mapToDTO(updated);
                    dto.setAmountPaidNow(amountPaidNow); // optional: set last paid amount in response
                    return dto;
                })
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
    }

}
