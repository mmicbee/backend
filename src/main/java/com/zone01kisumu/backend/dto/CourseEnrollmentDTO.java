package com.zone01kisumu.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// class CourseEnrollmentDTO is used to transfer course enrollment data between the service and controller layers
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseEnrollmentDTO {
    private Long id;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Enrollment date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime enrollmentDate;

    @NotBlank(message = "Payment status is required")
    private String paymentStatus;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @DecimalMin(value = "0.0", inclusive = true, message = "Amount paid must be non-negative")
    private BigDecimal amountPaidNow;

    @DecimalMin(value = "0.0", inclusive = true, message = "Amount remaining must be non-negative")
    private BigDecimal amountRemaining;

}
