package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.component.PerformanceMetricsCollector;
import com.zone01kisumu.backend.dto.MonitoringDTOs.ApiPerformanceStats;

class PerformanceMetricsCollectorTest {

    private PerformanceMetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new PerformanceMetricsCollector();
    }

    @Test
    void testRecordRequestsAndPercentiles() {
        collector.recordRequest(10L, false);
        collector.recordRequest(20L, false);
        collector.recordRequest(30L, false);
        collector.recordRequest(100L, true);

        ApiPerformanceStats stats = collector.getMetrics();

        assertNotNull(stats);
        assertEquals(4L, stats.getTotalRequests());
        assertEquals(1L, stats.getTotalErrors());
        assertEquals(25.0, stats.getErrorRatePercent());
        assertEquals(40.0, stats.getAvgResponseTimeMs());
        assertTrue(stats.getP95ResponseTimeMs() >= 30.0);
    }

    @Test
    void testEmptyMetrics() {
        ApiPerformanceStats stats = collector.getMetrics();
        assertEquals(0L, stats.getTotalRequests());
        assertEquals(0L, stats.getTotalErrors());
        assertEquals(0.0, stats.getErrorRatePercent());
        assertEquals(0.0, stats.getAvgResponseTimeMs());
        assertEquals(0.0, stats.getP95ResponseTimeMs());
        assertEquals(0.0, stats.getP99ResponseTimeMs());
    }

    @Test
    void testReset() {
        collector.recordRequest(50L, false);
        assertEquals(1L, collector.getMetrics().getTotalRequests());

        collector.reset();
        assertEquals(0L, collector.getMetrics().getTotalRequests());
    }
}
