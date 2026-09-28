package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.MonitoringDTOs.AlertEventDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.ApiPerformanceStats;
import com.zone01kisumu.backend.dto.MonitoringDTOs.DatabaseHealthStats;
import com.zone01kisumu.backend.dto.MonitoringDTOs.HeapMemoryStats;
import com.zone01kisumu.backend.dto.MonitoringDTOs.PipelineHealthDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.PipelinesSummaryDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemHealthDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemPerformanceStatsDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.ThreadStats;

class MonitoringDTOsTest {

    @Test
    void testPipelineHealthDTO() {
        LocalDateTime now = LocalDateTime.now();
        PipelineHealthDTO dto = PipelineHealthDTO.builder()
                .pipelineName("TEST_PIPELINE")
                .status("HEALTHY")
                .lastRunTime(now)
                .lastDurationMs(150L)
                .successRate(100.0)
                .totalRuns(10)
                .successfulRuns(10)
                .failedRuns(0)
                .recordsProcessed(500)
                .freshnessLagSeconds(60L)
                .errorMessage(null)
                .build();

        assertEquals("TEST_PIPELINE", dto.getPipelineName());
        assertEquals("HEALTHY", dto.getStatus());
        assertEquals(now, dto.getLastRunTime());
        assertEquals(150L, dto.getLastDurationMs());
        assertEquals(100.0, dto.getSuccessRate());
        assertEquals(10, dto.getTotalRuns());
        assertEquals(500, dto.getRecordsProcessed());
        assertEquals(60L, dto.getFreshnessLagSeconds());
        assertNotNull(dto.toString());
    }

    @Test
    void testPipelinesSummaryDTO() {
        LocalDateTime now = LocalDateTime.now();
        PipelinesSummaryDTO dto = PipelinesSummaryDTO.builder()
                .overallStatus("HEALTHY")
                .totalPipelines(4)
                .healthyCount(4)
                .degradedCount(0)
                .failedCount(0)
                .pipelines(List.of())
                .timestamp(now)
                .build();

        assertEquals("HEALTHY", dto.getOverallStatus());
        assertEquals(4, dto.getTotalPipelines());
        assertEquals(4, dto.getHealthyCount());
        assertNotNull(dto.toString());
    }

    @Test
    void testSystemPerformanceStatsDTO() {
        LocalDateTime now = LocalDateTime.now();
        HeapMemoryStats mem = HeapMemoryStats.builder()
                .usedBytes(1000L)
                .maxBytes(5000L)
                .freeBytes(4000L)
                .utilizationPercent(20.0)
                .build();

        ThreadStats threads = ThreadStats.builder()
                .activeThreads(10)
                .peakThreads(15)
                .totalStartedThreads(20L)
                .build();

        DatabaseHealthStats db = DatabaseHealthStats.builder()
                .status("UP")
                .queryLatencyMs(5L)
                .poolActiveConnections(1)
                .build();

        ApiPerformanceStats api = ApiPerformanceStats.builder()
                .totalRequests(100)
                .totalErrors(2)
                .errorRatePercent(2.0)
                .avgResponseTimeMs(15.0)
                .p95ResponseTimeMs(40.0)
                .p99ResponseTimeMs(50.0)
                .build();

        SystemPerformanceStatsDTO stats = SystemPerformanceStatsDTO.builder()
                .uptimeSeconds(3600L)
                .jvmMemory(mem)
                .threadStats(threads)
                .databaseStats(db)
                .apiMetrics(api)
                .timestamp(now)
                .build();

        assertEquals(3600L, stats.getUptimeSeconds());
        assertEquals(20.0, stats.getJvmMemory().getUtilizationPercent());
        assertEquals(10, stats.getThreadStats().getActiveThreads());
        assertEquals("UP", stats.getDatabaseStats().getStatus());
        assertEquals(2.0, stats.getApiMetrics().getErrorRatePercent());
        assertNotNull(stats.toString());
    }

    @Test
    void testAlertEventDTO() {
        LocalDateTime now = LocalDateTime.now();
        AlertEventDTO dto = AlertEventDTO.builder()
                .alertId("alert-1")
                .severity("WARNING")
                .component("PIPELINE")
                .title("Slow Pipeline")
                .message("Run took 6000ms")
                .metricValue(6000.0)
                .thresholdValue(5000.0)
                .acknowledged(false)
                .timestamp(now)
                .build();

        assertEquals("alert-1", dto.getAlertId());
        assertEquals("WARNING", dto.getSeverity());
        assertEquals("PIPELINE", dto.getComponent());
        assertEquals(6000.0, dto.getMetricValue());
        assertNotNull(dto.toString());
    }

    @Test
    void testSystemHealthDTO() {
        LocalDateTime now = LocalDateTime.now();
        SystemHealthDTO health = SystemHealthDTO.builder()
                .status("HEALTHY")
                .uptimeSeconds(7200L)
                .subsystems(Map.of("database", "UP", "memory", "HEALTHY"))
                .activeAlertsCount(0)
                .timestamp(now)
                .build();

        assertEquals("HEALTHY", health.getStatus());
        assertEquals(7200L, health.getUptimeSeconds());
        assertTrue(health.getSubsystems().containsKey("database"));
        assertEquals(0, health.getActiveAlertsCount());
        assertNotNull(health.toString());
    }
}
