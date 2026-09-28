package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.MonitoringAlert;

class MonitoringAlertEntityTest {

    @Test
    void testBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        MonitoringAlert alert = MonitoringAlert.builder()
                .id(1L)
                .alertId("alert-123")
                .severity(MonitoringAlert.AlertSeverity.CRITICAL)
                .component("DATABASE")
                .title("DB Latency High")
                .message("Latency exceeded 1000ms")
                .metricValue(1500.0)
                .thresholdValue(1000.0)
                .acknowledged(false)
                .createdAt(now)
                .build();

        assertEquals(1L, alert.getId());
        assertEquals("alert-123", alert.getAlertId());
        assertEquals(MonitoringAlert.AlertSeverity.CRITICAL, alert.getSeverity());
        assertEquals("DATABASE", alert.getComponent());
        assertEquals("DB Latency High", alert.getTitle());
        assertEquals("Latency exceeded 1000ms", alert.getMessage());
        assertEquals(1500.0, alert.getMetricValue());
        assertEquals(1000.0, alert.getThresholdValue());
        assertFalse(alert.isAcknowledged());
        assertEquals(now, alert.getCreatedAt());
        assertNotNull(alert.toString());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        MonitoringAlert alert = new MonitoringAlert();
        alert.setId(5L);
        alert.setAlertId("alert-999");
        alert.setAcknowledged(true);

        assertEquals(5L, alert.getId());
        assertEquals("alert-999", alert.getAlertId());
        assertTrue(alert.isAcknowledged());
    }

    @Test
    void testSeverityEnum() {
        assertEquals(MonitoringAlert.AlertSeverity.INFO, MonitoringAlert.AlertSeverity.valueOf("INFO"));
        assertEquals(MonitoringAlert.AlertSeverity.WARNING, MonitoringAlert.AlertSeverity.valueOf("WARNING"));
        assertEquals(MonitoringAlert.AlertSeverity.CRITICAL, MonitoringAlert.AlertSeverity.valueOf("CRITICAL"));
        assertTrue(MonitoringAlert.AlertSeverity.values().length >= 3);
    }
}
