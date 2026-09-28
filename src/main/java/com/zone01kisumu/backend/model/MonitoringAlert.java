package com.zone01kisumu.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an operational alert triggered by metric threshold violations
 * or system anomalies.
 */
@Entity
@Table(
        name = "monitoring_alerts",
        indexes = {
                @Index(name = "idx_alert_severity", columnList = "severity"),
                @Index(name = "idx_alert_created", columnList = "created_at"),
                @Index(name = "idx_alert_ack", columnList = "acknowledged")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitoringAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", nullable = false, unique = true, length = 64)
    private String alertId;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "component", nullable = false, length = 100)
    private String component;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "metric_value")
    private Double metricValue;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Builder.Default
    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AlertSeverity {
        INFO,
        WARNING,
        CRITICAL
    }
}
