package com.zone01kisumu.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.PerformanceSummary;

/**
 * Spring Data JPA Repository for PerformanceSummary entities.
 */
@Repository
public interface PerformanceSummaryRepository extends JpaRepository<PerformanceSummary, Long> {

    Optional<PerformanceSummary> findByReportId(String reportId);

    List<PerformanceSummary> findByReportTypeAndEntityId(
            PerformanceSummary.ReportType reportType, Long entityId);

    Page<PerformanceSummary> findByReportType(
            PerformanceSummary.ReportType reportType, Pageable pageable);

    Page<PerformanceSummary> findByReportTypeAndEntityId(
            PerformanceSummary.ReportType reportType, Long entityId, Pageable pageable);

    List<PerformanceSummary> findByDateGeneratedBetween(
            LocalDateTime startDate, LocalDateTime endDate);
}
