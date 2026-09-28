package com.zone01kisumu.backend.service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.zone01kisumu.backend.component.PerformanceMetricsCollector;
import com.zone01kisumu.backend.dto.MonitoringDTOs.ApiPerformanceStats;
import com.zone01kisumu.backend.dto.MonitoringDTOs.DatabaseHealthStats;
import com.zone01kisumu.backend.dto.MonitoringDTOs.HeapMemoryStats;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemHealthDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemPerformanceStatsDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.ThreadStats;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service collecting and reporting live system performance statistics,
 * JVM metrics, database latency, and composite system health.
 */
@Service
@RequiredArgsConstructor
public class PerformanceStatsService {

    private static final double MEMORY_WARNING_THRESHOLD_PERCENT = 90.0;
    private static final long DB_LATENCY_WARNING_THRESHOLD_MS = 1000L;
    private static final double API_ERROR_RATE_WARNING_THRESHOLD = 5.0;

    private final JdbcTemplate jdbcTemplate;
    private final PerformanceMetricsCollector metricsCollector;
    private final MonitoringAlertService alertService;

    /**
     * Gathers live system performance metrics.
     *
     * @return SystemPerformanceStatsDTO.
     */
    public SystemPerformanceStatsDTO getSystemPerformanceStats() {
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;
        HeapMemoryStats memoryStats = getHeapMemoryStats();
        ThreadStats threadStats = getThreadStats();
        DatabaseHealthStats dbStats = getDatabaseHealthStats();
        ApiPerformanceStats apiStats = metricsCollector.getMetrics();

        // Anomaly / Threshold checks
        if (memoryStats.getUtilizationPercent() > MEMORY_WARNING_THRESHOLD_PERCENT) {
            alertService.raiseAlert(
                    MonitoringAlert.AlertSeverity.WARNING,
                    "JVM_MEMORY",
                    "High Memory Utilization",
                    "JVM heap utilization reached " + memoryStats.getUtilizationPercent() + "%",
                    memoryStats.getUtilizationPercent(),
                    MEMORY_WARNING_THRESHOLD_PERCENT
            );
        }

        if (dbStats.getQueryLatencyMs() > DB_LATENCY_WARNING_THRESHOLD_MS) {
            alertService.raiseAlert(
                    MonitoringAlert.AlertSeverity.WARNING,
                    "DATABASE",
                    "Slow Database Query Latency",
                    "Database health query latency (" + dbStats.getQueryLatencyMs() + " ms) exceeded threshold",
                    (double) dbStats.getQueryLatencyMs(),
                    (double) DB_LATENCY_WARNING_THRESHOLD_MS
            );
        }

        if (apiStats.getErrorRatePercent() > API_ERROR_RATE_WARNING_THRESHOLD) {
            alertService.raiseAlert(
                    MonitoringAlert.AlertSeverity.WARNING,
                    "API_METRICS",
                    "Elevated API Error Rate",
                    "API error rate (" + apiStats.getErrorRatePercent() + "%) exceeded threshold",
                    apiStats.getErrorRatePercent(),
                    API_ERROR_RATE_WARNING_THRESHOLD
            );
        }

        return SystemPerformanceStatsDTO.builder()
                .uptimeSeconds(uptimeSeconds)
                .jvmMemory(memoryStats)
                .threadStats(threadStats)
                .databaseStats(dbStats)
                .apiMetrics(apiStats)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Evaluates composite system health across memory, database, and operational alerts.
     *
     * @return SystemHealthDTO.
     */
    public SystemHealthDTO getSystemHealth() {
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;
        DatabaseHealthStats dbStats = getDatabaseHealthStats();
        HeapMemoryStats memoryStats = getHeapMemoryStats();
        int activeAlerts = alertService.getActiveAlertsCount();

        Map<String, String> subsystems = new HashMap<>();
        subsystems.put("database", dbStats.getStatus());
        subsystems.put("jvm_memory", memoryStats.getUtilizationPercent() > 90.0 ? "DEGRADED" : "HEALTHY");
        subsystems.put("api_metrics", metricsCollector.getMetrics().getErrorRatePercent() > 5.0
                ? "DEGRADED" : "HEALTHY");

        String status = "HEALTHY";
        if ("DOWN".equalsIgnoreCase(dbStats.getStatus())) {
            status = "CRITICAL";
        } else if (activeAlerts > 0 || subsystems.containsValue("DEGRADED")) {
            status = "DEGRADED";
        }

        return SystemHealthDTO.builder()
                .status(status)
                .uptimeSeconds(uptimeSeconds)
                .subsystems(subsystems)
                .activeAlertsCount(activeAlerts)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private HeapMemoryStats getHeapMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        double utilization = (maxMemory > 0) ? ((double) usedMemory / maxMemory) * 100.0 : 0.0;

        return HeapMemoryStats.builder()
                .usedBytes(usedMemory)
                .maxBytes(maxMemory)
                .freeBytes(freeMemory)
                .utilizationPercent(Math.round(utilization * 100.0) / 100.0)
                .build();
    }

    private ThreadStats getThreadStats() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return ThreadStats.builder()
                .activeThreads(threadBean.getThreadCount())
                .peakThreads(threadBean.getPeakThreadCount())
                .totalStartedThreads(threadBean.getTotalStartedThreadCount())
                .build();
    }

    private DatabaseHealthStats getDatabaseHealthStats() {
        long start = System.currentTimeMillis();
        String status = "UP";
        try {
            jdbcTemplate.execute("SELECT 1");
        } catch (Exception e) {
            status = "DOWN";
            LoggerUtil.logError("Database health query failed", e);
        }
        long latency = System.currentTimeMillis() - start;

        return DatabaseHealthStats.builder()
                .status(status)
                .queryLatencyMs(latency)
                .poolActiveConnections(1)
                .build();
    }
}
