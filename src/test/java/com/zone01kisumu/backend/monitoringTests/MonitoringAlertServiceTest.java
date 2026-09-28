package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zone01kisumu.backend.dto.MonitoringDTOs.AlertEventDTO;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.repository.MonitoringAlertRepository;
import com.zone01kisumu.backend.service.MonitoringAlertService;

@ExtendWith(MockitoExtension.class)
class MonitoringAlertServiceTest {

    @Mock
    private MonitoringAlertRepository alertRepository;

    @InjectMocks
    private MonitoringAlertService alertService;

    private MonitoringAlert mockAlert;

    @BeforeEach
    void setUp() {
        mockAlert = MonitoringAlert.builder()
                .id(1L)
                .alertId("alert-123")
                .severity(MonitoringAlert.AlertSeverity.WARNING)
                .component("PIPELINE")
                .title("Slow Pipeline")
                .message("Took 6000ms")
                .metricValue(6000.0)
                .thresholdValue(5000.0)
                .acknowledged(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void raiseAlert_savesAlert() {
        when(alertRepository.save(any(MonitoringAlert.class))).thenReturn(mockAlert);

        MonitoringAlert result = alertService.raiseAlert(
                MonitoringAlert.AlertSeverity.WARNING,
                "PIPELINE",
                "Slow Pipeline",
                "Took 6000ms",
                6000.0,
                5000.0
        );

        assertNotNull(result);
        assertEquals("alert-123", result.getAlertId());
        verify(alertRepository).save(any(MonitoringAlert.class));
    }

    @Test
    void getActiveAlerts_returnsList() {
        when(alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc()).thenReturn(List.of(mockAlert));

        List<AlertEventDTO> active = alertService.getActiveAlerts();

        assertEquals(1, active.size());
        assertEquals("alert-123", active.get(0).getAlertId());
        assertEquals("WARNING", active.get(0).getSeverity());
    }

    @Test
    void getAlertsBySeverity_returnsList() {
        when(alertRepository.findBySeverityOrderByCreatedAtDesc(MonitoringAlert.AlertSeverity.WARNING))
                .thenReturn(List.of(mockAlert));

        List<AlertEventDTO> alerts = alertService.getAlertsBySeverity(MonitoringAlert.AlertSeverity.WARNING);

        assertEquals(1, alerts.size());
    }

    @Test
    void getAllRecentAlerts_returnsList() {
        when(alertRepository.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of(mockAlert));

        List<AlertEventDTO> alerts = alertService.getAllRecentAlerts();

        assertEquals(1, alerts.size());
    }

    @Test
    void acknowledgeAlert_returnsTrue_whenFound() {
        when(alertRepository.findByAlertId("alert-123")).thenReturn(Optional.of(mockAlert));

        boolean success = alertService.acknowledgeAlert("alert-123");

        assertTrue(success);
        assertTrue(mockAlert.isAcknowledged());
        verify(alertRepository).save(mockAlert);
    }

    @Test
    void acknowledgeAlert_returnsFalse_whenNotFound() {
        when(alertRepository.findByAlertId("unknown")).thenReturn(Optional.empty());

        boolean success = alertService.acknowledgeAlert("unknown");

        assertFalse(success);
    }

    @Test
    void clearAllAlerts_deletesAll() {
        alertService.clearAllAlerts();
        verify(alertRepository).deleteAll();
    }

    @Test
    void getActiveAlertsCount_returnsCount() {
        when(alertRepository.countByAcknowledgedFalse()).thenReturn(3L);

        assertEquals(3, alertService.getActiveAlertsCount());
    }
}
