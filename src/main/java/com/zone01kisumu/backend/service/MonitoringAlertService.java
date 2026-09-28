package com.zone01kisumu.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zone01kisumu.backend.dto.MonitoringDTOs.AlertEventDTO;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.repository.MonitoringAlertRepository;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service managing operational alerts, threshold violations, and incident notifications.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MonitoringAlertService {

    private final MonitoringAlertRepository alertRepository;

    /**
     * Raises an operational alert.
     *
     * @param severity       Severity level (INFO, WARNING, CRITICAL).
     * @param component      Affected component name.
     * @param title          Brief alert title.
     * @param message        Detailed description.
     * @param metricValue    Observed metric value.
     * @param thresholdValue Configured threshold.
     * @return Created MonitoringAlert entity.
     */
    public MonitoringAlert raiseAlert(
            MonitoringAlert.AlertSeverity severity,
            String component,
            String title,
            String message,
            Double metricValue,
            Double thresholdValue) {

        MonitoringAlert alert = MonitoringAlert.builder()
                .alertId(UUID.randomUUID().toString())
                .severity(severity)
                .component(component)
                .title(title)
                .message(message)
                .metricValue(metricValue)
                .thresholdValue(thresholdValue)
                .acknowledged(false)
                .build();

        MonitoringAlert saved = alertRepository.save(alert);
        LoggerUtil.logWarn("ALERT RAISED [{}] [{}] {}: {}", severity, component, title, message);
        return saved;
    }

    /**
     * Retrieves all active (unacknowledged) alerts.
     *
     * @return List of AlertEventDTO.
     */
    @Transactional(readOnly = true)
    public List<AlertEventDTO> getActiveAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves alerts filtered by severity.
     *
     * @param severity Target severity.
     * @return List of AlertEventDTO.
     */
    @Transactional(readOnly = true)
    public List<AlertEventDTO> getAlertsBySeverity(MonitoringAlert.AlertSeverity severity) {
        return alertRepository.findBySeverityOrderByCreatedAtDesc(severity).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the top 50 most recent alerts.
     *
     * @return List of AlertEventDTO.
     */
    @Transactional(readOnly = true)
    public List<AlertEventDTO> getAllRecentAlerts() {
        return alertRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Acknowledges an alert by ID.
     *
     * @param alertId UUID of the alert.
     * @return true if found and acknowledged, false otherwise.
     */
    public boolean acknowledgeAlert(String alertId) {
        return alertRepository.findByAlertId(alertId).map(alert -> {
            alert.setAcknowledged(true);
            alertRepository.save(alert);
            LoggerUtil.logInfo("Alert [{}] acknowledged", alertId);
            return true;
        }).orElse(false);
    }

    /**
     * Clears all alerts from the database.
     */
    public void clearAllAlerts() {
        alertRepository.deleteAll();
        LoggerUtil.logInfo("All monitoring alerts cleared");
    }

    /**
     * Returns total active unacknowledged alert count.
     *
     * @return count of active alerts.
     */
    @Transactional(readOnly = true)
    public int getActiveAlertsCount() {
        return (int) alertRepository.countByAcknowledgedFalse();
    }

    private AlertEventDTO mapToDTO(MonitoringAlert alert) {
        return AlertEventDTO.builder()
                .alertId(alert.getAlertId())
                .severity(alert.getSeverity() != null ? alert.getSeverity().name() : null)
                .component(alert.getComponent())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .metricValue(alert.getMetricValue())
                .thresholdValue(alert.getThresholdValue())
                .acknowledged(alert.isAcknowledged())
                .timestamp(alert.getCreatedAt())
                .build();
    }
}
