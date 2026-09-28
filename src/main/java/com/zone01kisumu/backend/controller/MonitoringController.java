package com.zone01kisumu.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.dto.MonitoringDTOs.AlertEventDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.PipelinesSummaryDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemHealthDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.SystemPerformanceStatsDTO;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.model.PipelineExecutionLog;
import com.zone01kisumu.backend.service.MonitoringAlertService;
import com.zone01kisumu.backend.service.PerformanceStatsService;
import com.zone01kisumu.backend.service.ReportPipelineMonitoringService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller providing telemetry, data pipeline monitoring, performance stats,
 * and operational alerting for platform administrators and instructors.
 */
@RestController
@RequestMapping("/api/admin/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final ReportPipelineMonitoringService pipelineMonitoringService;
    private final PerformanceStatsService performanceStatsService;
    private final MonitoringAlertService alertService;

    /**
     * Retrieves health and throughput summary across all report data pipelines.
     *
     * @return 200 OK with PipelinesSummaryDTO.
     */
    @GetMapping("/pipelines")
    public ResponseEntity<PipelinesSummaryDTO> getPipelinesSummary() {
        return ResponseEntity.ok(pipelineMonitoringService.getPipelinesSummary());
    }

    /**
     * Retrieves recent execution history for a specific pipeline.
     *
     * @param pipelineName Name of the pipeline.
     * @return 200 OK with list of execution logs.
     */
    @GetMapping("/pipelines/{pipelineName}/history")
    public ResponseEntity<List<PipelineExecutionLog>> getPipelineHistory(
            @PathVariable String pipelineName) {
        return ResponseEntity.ok(pipelineMonitoringService.getPipelineHistory(pipelineName));
    }

    /**
     * Retrieves live system performance statistics (JVM, threads, DB, API latency).
     *
     * @return 200 OK with SystemPerformanceStatsDTO.
     */
    @GetMapping("/performance")
    public ResponseEntity<SystemPerformanceStatsDTO> getPerformanceStats() {
        return ResponseEntity.ok(performanceStatsService.getSystemPerformanceStats());
    }

    /**
     * Retrieves operational alerts with optional severity filter.
     *
     * @param severity Optional severity filter (INFO, WARNING, CRITICAL).
     * @return 200 OK with list of AlertEventDTO.
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<AlertEventDTO>> getAlerts(
            @RequestParam(required = false) MonitoringAlert.AlertSeverity severity) {
        if (severity != null) {
            return ResponseEntity.ok(alertService.getAlertsBySeverity(severity));
        }
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    /**
     * Acknowledges an alert.
     *
     * @param alertId UUID of the alert.
     * @return 200 OK on success, 404 if alert not found.
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable String alertId) {
        boolean ack = alertService.acknowledgeAlert(alertId);
        return ack ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Clears all alerts.
     *
     * @return 200 OK.
     */
    @PostMapping("/alerts/clear")
    public ResponseEntity<Void> clearAlerts() {
        alertService.clearAllAlerts();
        return ResponseEntity.ok().build();
    }

    /**
     * Evaluates composite platform health across sub-systems.
     *
     * @return 200 OK with SystemHealthDTO (or 503 if critical).
     */
    @GetMapping("/health")
    public ResponseEntity<SystemHealthDTO> getSystemHealth() {
        SystemHealthDTO health = performanceStatsService.getSystemHealth();
        if ("CRITICAL".equalsIgnoreCase(health.getStatus())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
        return ResponseEntity.ok(health);
    }
}
