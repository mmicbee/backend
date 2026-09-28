package com.zone01kisumu.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// class CourseDTO is used to transfer course data between the service and controller layers
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {

  private Long id;

  @NotBlank(message = "Course title is required")
  @Size(max = 255, message = "Title must not exceed 255 characters")
  private String title;

  private String description;

  @NotBlank(message = "Category is required")
  private String category;

  @NotNull(message = "Duration is required")
  @Min(value = 1, message = "Duration must be at least 1")
  private Integer duration;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  @NotBlank(message = "Mode is required")
  private String mode;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
  private BigDecimal price;

  private Long teacherId;

  private String instructorName;

  private String paymentMethod;

  private String mpesaPaymentType;

  @Size(max = 100, message = "Paybill number must not exceed 100 characters")
  private String paybillNumber; // Only used for paybill(business number)

  @Size(max = 255, message = "Payment account must not exceed 255 characters")
  private String paymentAccount;

  private LocalDateTime createdAt;

  private String status;

  public CourseDTO(
      Long id,
      String title,
      String description,
      String category,
      Integer duration,
      LocalDateTime startDate,
      LocalDateTime endDate,
      String mode,
      BigDecimal price,
      Long teacherId,
      String paymentMethod,
      String mpesaPaymentType,
      String paybillNumber,
      String paymentAccount,
      LocalDateTime createdAt,
      String status) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.category = category;
    this.duration = duration;
    this.startDate = startDate;
    this.endDate = endDate;
    this.mode = mode;
    this.price = price;
    this.teacherId = teacherId;
    this.instructorName = null;
    this.paymentMethod = paymentMethod;
    this.mpesaPaymentType = mpesaPaymentType;
    this.paybillNumber = paybillNumber;
    this.paymentAccount = paymentAccount;
    this.createdAt = createdAt;
    this.status = status;
  }
}
