package com.zone01kisumu.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entity class for Course Enrollment
@Entity
@Table(name = "course_enrollment", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id", "course_id" }))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreationTimestamp
    @Column(name = "enrollment_date", updatable = false)
    private LocalDateTime enrollmentDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

    @Column(name = "amount_due")
    private BigDecimal amountDue;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    public enum PaymentStatus {
        PENDING, PARTIALLY_PAID, COMPLETED, FAILED
    }

    public enum PaymentMethod {
        MPESA, CREDIT_CARD, PAYPAL, BANK_TRANSFER
    }

    public CourseEnrollment(Long id) {
        this.id = id;
    }
}
