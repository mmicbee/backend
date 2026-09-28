package com.zone01kisumu.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for Data Pipeline Telemetry, Performance Statistics, and Alerting.
 */
public class MonitoringDTOs {

    /**
     * Health and metrics snapshot for a single data pipeline.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PipelineHealthDTO {
        private String pipelineName;
        private String status;
        private LocalDateTime lastRunTime;
        private Long lastDurationMs;
        private double successRate;
        private long totalRuns;
        private long successfulRuns;
        private long failedRuns;
        private long recordsProcessed;
        private Long freshnessLagSeconds;
        private String errorMessage;
    }

    /**
     * Aggregated summary of all data pipelines across the LMS platform.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PipelinesSummaryDTO {
        private String overallStatus;
        private int totalPipelines;
        private int healthyCount;
        private int degradedCount;
        private int failedCount;
        private List<PipelineHealthDTO> pipelines;
        private LocalDateTime timestamp;
    }

    /**
     * Live system performance telemetry including JVM, threads, database, and API latency.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemPerformanceStatsDTO {
        private long uptimeSeconds;
        private HeapMemoryStats jvmMemory;
        private ThreadStats threadStats;
        private DatabaseHealthStats databaseStats;
        private ApiPerformanceStats apiMetrics;
        private LocalDateTime timestamp;
    }

    /**
     * JVM Heap Memory utilization metrics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeapMemoryStats {
        private long usedBytes;
        private long maxBytes;
        private long freeBytes;
        private double utilizationPercent;
    }

    /**
     * JVM Thread metrics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadStats {
        private int activeThreads;
        private int peakThreads;
        private long totalStartedThreads;
    }

    /**
     * Database connectivity and query latency telemetry.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseHealthStats {
        private String status;
        private long queryLatencyMs;
        private int poolActiveConnections;
    }

    /**
     * API request latency, throughput, and error metrics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiPerformanceStats {
        private long totalRequests;
        private long totalErrors;
        private double errorRatePercent;
        private double avgResponseTimeMs;
        private double p95ResponseTimeMs;
        private double p99ResponseTimeMs;
    }

    /**
     * Structured alert event for threshold violations or anomalies.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertEventDTO {
        private String alertId;
        private String severity;
        private String component;
        private String title;
        private String message;
        private Double metricValue;
        private Double thresholdValue;
        private boolean acknowledged;
        private LocalDateTime timestamp;
    }

    /**
     * Overall platform health composite status.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealthDTO {
        private String status;
        private long uptimeSeconds;
        private Map<String, String> subsystems;
        private int activeAlertsCount;
        private LocalDateTime timestamp;
    }
}
