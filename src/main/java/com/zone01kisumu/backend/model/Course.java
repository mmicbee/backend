package com.zone01kisumu.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

// Entity class for Courses
@Entity
@Table(
    name = "course",
    indexes = {
      @Index(name = "idx_course_title", columnList = "title"),
      @Index(name = "idx_course_category", columnList = "category"),
      @Index(name = "idx_course_mode", columnList = "mode"),
      @Index(name = "idx_course_status", columnList = "status"),
      @Index(name = "idx_course_price", columnList = "price"),
      @Index(name = "idx_course_teacher_id", columnList = "teacher_id"),
      @Index(name = "idx_course_created_at", columnList = "created_at")
    })
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course {

  @Id
  @Column(nullable = false, unique = true)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Convert(converter = CourseCategoryConverter.class)
  @Column(name = "category", nullable = false)
  private Category category;

  @Builder.Default
  @Column(nullable = false)
  private Integer duration = 0;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Mode mode;

  // @Builder.Default
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "teacher_id", nullable = true)
  private Long teacherId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false)
  private PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

  @Enumerated(EnumType.STRING)
  @Column(name = "mpesa_payment_type")
  private MpesaPaymentType mpesaPaymentType;

  @Column(name = "paybill_number", length = 100)
  private String paybillNumber; // Only used for paybill(business number)

  @Column(name = "payment_account", length = 255)
  private String paymentAccount;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status = Status.OPEN;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CourseTopic> courseTopic;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CourseEnrollment> enrollments;

  public enum Mode {
    LIVE,
    RECORDED
  }

  public enum PaymentMethod {
    MPESA,
    CREDIT_CARD,
    PAYPAL,
    BANK_TRANSFER
  }

  public enum MpesaPaymentType {
    PAYBILL,
    TILL
  }

  public enum Category {
    SCIENCE_AND_TECHNOLOGY,
    ARTS_AND_DESIGN,
    BUSINESS,
    LANGUAGES,
    MARKETING,
    SOFTWARE_AND_DATA,
    HEALTH_AND_FITNESS,
    MEDIA_AND_ENTERTAINMENT,
    OTHERS;

    public static Category fromString(String value) {
      if (value == null || value.isBlank()) {
        return OTHERS;
      }
      String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
      for (Category cat : Category.values()) {
        if (cat.name().equals(normalized)) {
          return cat;
        }
      }
      if (normalized.contains("WEB")
          || normalized.contains("DEV")
          || normalized.contains("SOFTWARE")
          || normalized.contains("DATA")
          || normalized.contains("TECH")
          || normalized.contains("PROGRAM")) {
        return Category.SOFTWARE_AND_DATA;
      }
      if (normalized.contains("BUSINESS")
          || normalized.contains("FINANCE")
          || normalized.contains("MANAGEMENT")) {
        return Category.BUSINESS;
      }
      if (normalized.contains("DESIGN") || normalized.contains("ART")) {
        return Category.ARTS_AND_DESIGN;
      }
      return Category.OTHERS;
    }
  }

  public enum Status {
    OPEN,
    STARTED,
    COMPLETED,
    CANCELLED,
    CLOSED
  }

  // Getters and Setters
  public Course(Long id) {
    this.id = id;
  }
}
