package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zone01kisumu.backend.component.PerformanceMetricsCollector;
import com.zone01kisumu.backend.dto.MonitoringDTOs.ApiPerformanceStats;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemHealthDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemPerformanceStatsDTO;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.service.MonitoringAlertService;
import com.zone01kisumu.backend.service.PerformanceStatsService;

@ExtendWith(MockitoExtension.class)
class PerformanceStatsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PerformanceMetricsCollector metricsCollector;

    @Mock
    private MonitoringAlertService alertService;

    @InjectMocks
    private PerformanceStatsService performanceStatsService;

    private ApiPerformanceStats mockApiStats;

    @BeforeEach
    void setUp() {
        mockApiStats = ApiPerformanceStats.builder()
                .totalRequests(100L)
                .totalErrors(1L)
                .errorRatePercent(1.0)
                .avgResponseTimeMs(20.0)
                .p95ResponseTimeMs(45.0)
                .p99ResponseTimeMs(60.0)
                .build();
    }

    @Test
    void getSystemPerformanceStats_success() {
        when(metricsCollector.getMetrics()).thenReturn(mockApiStats);

        SystemPerformanceStatsDTO stats = performanceStatsService.getSystemPerformanceStats();

        assertNotNull(stats);
        assertNotNull(stats.getJvmMemory());
        assertNotNull(stats.getThreadStats());
        assertEquals("UP", stats.getDatabaseStats().getStatus());
        assertEquals(1.0, stats.getApiMetrics().getErrorRatePercent());
    }

    @Test
    void getSystemPerformanceStats_highErrorRate_triggersAlert() {
        ApiPerformanceStats highErrorStats = ApiPerformanceStats.builder()
                .totalRequests(100L)
                .totalErrors(10L)
                .errorRatePercent(10.0)
                .avgResponseTimeMs(20.0)
                .p95ResponseTimeMs(45.0)
                .p99ResponseTimeMs(60.0)
                .build();
        when(metricsCollector.getMetrics()).thenReturn(highErrorStats);

        SystemPerformanceStatsDTO stats = performanceStatsService.getSystemPerformanceStats();

        assertNotNull(stats);
        verify(alertService).raiseAlert(
                eq(MonitoringAlert.AlertSeverity.WARNING),
                eq("API_METRICS"),
                any(),
                any(),
                eq(10.0),
                eq(5.0)
        );
    }

    @Test
    void getSystemHealth_healthy() {
        when(metricsCollector.getMetrics()).thenReturn(mockApiStats);
        when(alertService.getActiveAlertsCount()).thenReturn(0);

        SystemHealthDTO health = performanceStatsService.getSystemHealth();

        assertNotNull(health);
        assertEquals("HEALTHY", health.getStatus());
        assertEquals("UP", health.getSubsystems().get("database"));
    }

    @Test
    void getSystemHealth_dbDown_critical() {
        doThrow(new RuntimeException("DB offline")).when(jdbcTemplate).execute(anyString());
        when(metricsCollector.getMetrics()).thenReturn(mockApiStats);
        when(alertService.getActiveAlertsCount()).thenReturn(0);

        SystemHealthDTO health = performanceStatsService.getSystemHealth();

        assertNotNull(health);
        assertEquals("CRITICAL", health.getStatus());
        assertEquals("DOWN", health.getSubsystems().get("database"));
    }

    @Test
    void getSystemHealth_activeAlerts_degraded() {
        when(metricsCollector.getMetrics()).thenReturn(mockApiStats);
        when(alertService.getActiveAlertsCount()).thenReturn(2);

        SystemHealthDTO health = performanceStatsService.getSystemHealth();

        assertNotNull(health);
        assertEquals("DEGRADED", health.getStatus());
    }
}
