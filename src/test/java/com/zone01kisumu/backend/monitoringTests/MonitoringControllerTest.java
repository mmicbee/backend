package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.zone01kisumu.backend.controller.MonitoringController;
import com.zone01kisumu.backend.dto.MonitoringDTOs.AlertEventDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.PipelinesSummaryDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemHealthDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemPerformanceStatsDTO;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.model.PipelineExecutionLog;
import com.zone01kisumu.backend.service.MonitoringAlertService;
import com.zone01kisumu.backend.service.PerformanceStatsService;
import com.zone01kisumu.backend.service.ReportPipelineMonitoringService;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    @Mock
    private ReportPipelineMonitoringService pipelineMonitoringService;

    @Mock
    private PerformanceStatsService performanceStatsService;

    @Mock
    private MonitoringAlertService alertService;

    @InjectMocks
    private MonitoringController monitoringController;

    @Test
    void getPipelinesSummary_returnsOk() {
        PipelinesSummaryDTO summary = PipelinesSummaryDTO.builder()
                .overallStatus("HEALTHY")
                .totalPipelines(4)
                .healthyCount(4)
                .build();

        when(pipelineMonitoringService.getPipelinesSummary()).thenReturn(summary);

        ResponseEntity<PipelinesSummaryDTO> response = monitoringController.getPipelinesSummary();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HEALTHY", response.getBody().getOverallStatus());
    }

    @Test
    void getPipelineHistory_returnsOk() {
        PipelineExecutionLog log = PipelineExecutionLog.builder()
                .pipelineName(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE)
                .build();

        when(pipelineMonitoringService.getPipelineHistory(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE))
                .thenReturn(List.of(log));

        ResponseEntity<List<PipelineExecutionLog>> response =
                monitoringController.getPipelineHistory(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getPerformanceStats_returnsOk() {
        SystemPerformanceStatsDTO stats = SystemPerformanceStatsDTO.builder()
                .uptimeSeconds(3600L)
                .timestamp(LocalDateTime.now())
                .build();

        when(performanceStatsService.getSystemPerformanceStats()).thenReturn(stats);

        ResponseEntity<SystemPerformanceStatsDTO> response = monitoringController.getPerformanceStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3600L, response.getBody().getUptimeSeconds());
    }

    @Test
    void getAlerts_withoutSeverity_returnsActiveAlerts() {
        AlertEventDTO alert = AlertEventDTO.builder().alertId("alert-1").build();
        when(alertService.getActiveAlerts()).thenReturn(List.of(alert));

        ResponseEntity<List<AlertEventDTO>> response = monitoringController.getAlerts(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getAlerts_withSeverity_returnsFilteredAlerts() {
        AlertEventDTO alert = AlertEventDTO.builder().alertId("alert-2").severity("WARNING").build();
        when(alertService.getAlertsBySeverity(MonitoringAlert.AlertSeverity.WARNING)).thenReturn(List.of(alert));

        ResponseEntity<List<AlertEventDTO>> response =
                monitoringController.getAlerts(MonitoringAlert.AlertSeverity.WARNING);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("WARNING", response.getBody().get(0).getSeverity());
    }

    @Test
    void acknowledgeAlert_returnsOk_whenFound() {
        when(alertService.acknowledgeAlert("alert-1")).thenReturn(true);

        ResponseEntity<Void> response = monitoringController.acknowledgeAlert("alert-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void acknowledgeAlert_returnsNotFound_whenMissing() {
        when(alertService.acknowledgeAlert("unknown")).thenReturn(false);

        ResponseEntity<Void> response = monitoringController.acknowledgeAlert("unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void clearAlerts_returnsOk() {
        ResponseEntity<Void> response = monitoringController.clearAlerts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(alertService).clearAllAlerts();
    }

    @Test
    void getSystemHealth_returnsOk_whenHealthy() {
        SystemHealthDTO health = SystemHealthDTO.builder()
                .status("HEALTHY")
                .subsystems(Map.of("database", "UP"))
                .build();

        when(performanceStatsService.getSystemHealth()).thenReturn(health);

        ResponseEntity<SystemHealthDTO> response = monitoringController.getSystemHealth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HEALTHY", response.getBody().getStatus());
    }

    @Test
    void getSystemHealth_returnsServiceUnavailable_whenCritical() {
        SystemHealthDTO health = SystemHealthDTO.builder()
                .status("CRITICAL")
                .subsystems(Map.of("database", "DOWN"))
                .build();

        when(performanceStatsService.getSystemHealth()).thenReturn(health);

        ResponseEntity<SystemHealthDTO> response = monitoringController.getSystemHealth();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}
