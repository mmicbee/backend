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
 * Entity tracking individual execution instances of report and data pipelines.
 */
@Entity
@Table(
        name = "pipeline_execution_log",
        indexes = {
                @Index(name = "idx_pipeline_name_start", columnList = "pipeline_name, start_time"),
                @Index(name = "idx_pipeline_status", columnList = "execution_status")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PipelineExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pipeline_name", nullable = false, length = 100)
    private String pipelineName;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 30)
    private PipelineStatus executionStatus;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "records_processed")
    private Long recordsProcessed;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "freshness_timestamp")
    private LocalDateTime freshnessTimestamp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum PipelineStatus {
        SUCCESS,
        FAILED,
        RUNNING,
        DEGRADED
    }
}
