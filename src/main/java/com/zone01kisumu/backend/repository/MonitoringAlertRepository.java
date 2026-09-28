package com.zone01kisumu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.MonitoringAlert;

/**
 * Spring Data JPA Repository for MonitoringAlert.
 */
@Repository
public interface MonitoringAlertRepository extends JpaRepository<MonitoringAlert, Long> {

    Optional<MonitoringAlert> findByAlertId(String alertId);

    List<MonitoringAlert> findByAcknowledgedFalseOrderByCreatedAtDesc();

    List<MonitoringAlert> findBySeverityOrderByCreatedAtDesc(
            MonitoringAlert.AlertSeverity severity);

    List<MonitoringAlert> findTop50ByOrderByCreatedAtDesc();

    long countByAcknowledgedFalse();
}
