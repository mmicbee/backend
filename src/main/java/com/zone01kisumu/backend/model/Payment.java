package com.zone01kisumu.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Entity Classes
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "student_id", nullable = false)
    private Long studentId;

    @JoinColumn(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.MPESA;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "paystack_reference")
    private String paystackReference;

    @Column(name = "paystack_access_code")
    private String paystackAccessCode;

    // Constructors
    public Payment(Long studentId, Long courseId, BigDecimal amount,
            PaymentMethod paymentMethod, String paystackReference) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paystackReference = paystackReference;
    }

    public enum PaymentMethod {
        MPESA, CREDIT_CARD, PAYPAL, BANK_TRANSFER
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }
}