package com.zone01kisumu.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zone01kisumu.backend.dto.MonitoringDTOs.PipelineHealthDTO;
import com.zone01kisumu.backend.dto.MonitoringDTOs.PipelinesSummaryDTO;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.model.PipelineExecutionLog;
import com.zone01kisumu.backend.repository.PipelineExecutionLogRepository;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service managing report data pipeline health, telemetry recording, and freshness tracking.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReportPipelineMonitoringService {

    public static final String CLASS_REPORT_PIPELINE = "CLASS_PERFORMANCE_REPORT_PIPELINE";
    public static final String STUDENT_REPORT_PIPELINE = "STUDENT_PERFORMANCE_REPORT_PIPELINE";
    public static final String COURSE_REPORT_PIPELINE = "COURSE_PERFORMANCE_REPORT_PIPELINE";
    public static final String CONTENT_BACKUP_PIPELINE = "CONTENT_BACKUP_PIPELINE";

    private static final long DURATION_WARNING_THRESHOLD_MS = 5000L;
    private static final long FRESHNESS_LAG_WARNING_SECONDS = 86400L; // 24 hours

    private final PipelineExecutionLogRepository pipelineLogRepository;
    private final MonitoringAlertService alertService;

    /**
     * Records a pipeline execution run.
     *
     * @param pipelineName     Name of the pipeline.
     * @param durationMs       Run duration in milliseconds.
     * @param recordsProcessed Number of records processed.
     * @param success          Whether the execution succeeded.
     * @param errorMessage     Optional error message if failed.
     * @return Saved PipelineExecutionLog entity.
     */
    public PipelineExecutionLog recordPipelineExecution(
            String pipelineName,
            long durationMs,
            long recordsProcessed,
            boolean success,
            String errorMessage) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusNanos(durationMs * 1_000_000L);

        PipelineExecutionLog.PipelineStatus status = success
                ? (durationMs > DURATION_WARNING_THRESHOLD_MS
                ? PipelineExecutionLog.PipelineStatus.DEGRADED
                : PipelineExecutionLog.PipelineStatus.SUCCESS)
                : PipelineExecutionLog.PipelineStatus.FAILED;

        PipelineExecutionLog log = PipelineExecutionLog.builder()
                .pipelineName(pipelineName)
                .executionStatus(status)
                .startTime(startTime)
                .endTime(now)
                .durationMs(durationMs)
                .recordsProcessed(recordsProcessed)
                .errorMessage(errorMessage)
                .freshnessTimestamp(now)
                .build();

        PipelineExecutionLog saved = pipelineLogRepository.save(log);

        // Alert checks
        if (!success) {
            alertService.raiseAlert(
                    MonitoringAlert.AlertSeverity.CRITICAL,
                    pipelineName,
                    "Pipeline Execution Failed",
                    "Pipeline [" + pipelineName + "] failed with error: " + errorMessage,
                    (double) durationMs,
                    (double) DURATION_WARNING_THRESHOLD_MS
            );
        } else if (durationMs > DURATION_WARNING_THRESHOLD_MS) {
            alertService.raiseAlert(
                    MonitoringAlert.AlertSeverity.WARNING,
                    pipelineName,
                    "Slow Pipeline Execution",
                    "Pipeline [" + pipelineName + "] execution duration (" + durationMs
                            + " ms) exceeded threshold (" + DURATION_WARNING_THRESHOLD_MS + " ms)",
                    (double) durationMs,
                    (double) DURATION_WARNING_THRESHOLD_MS
            );
        }

        LoggerUtil.logInfo("Pipeline [{}] execution recorded: status={}, duration={} ms, records={}",
                pipelineName, status, durationMs, recordsProcessed);

        return saved;
    }

    /**
     * Gathers aggregated health and performance metrics across all report pipelines.
     *
     * @return PipelinesSummaryDTO.
     */
    @Transactional(readOnly = true)
    public PipelinesSummaryDTO getPipelinesSummary() {
        Set<String> knownPipelines = new LinkedHashSet<>(List.of(
                CLASS_REPORT_PIPELINE,
                STUDENT_REPORT_PIPELINE,
                COURSE_REPORT_PIPELINE,
                CONTENT_BACKUP_PIPELINE
        ));

        // Include any additional dynamic pipelines from database logs
        pipelineLogRepository.findAll().forEach(l -> knownPipelines.add(l.getPipelineName()));

        List<PipelineHealthDTO> pipelineSummaries = new ArrayList<>();
        int healthyCount = 0;
        int degradedCount = 0;
        int failedCount = 0;

        LocalDateTime now = LocalDateTime.now();

        for (String name : knownPipelines) {
            PipelineHealthDTO health = getPipelineHealth(name, now);
            pipelineSummaries.add(health);

            if ("SUCCESS".equalsIgnoreCase(health.getStatus()) || "HEALTHY".equalsIgnoreCase(health.getStatus())) {
                healthyCount++;
            } else if ("DEGRADED".equalsIgnoreCase(health.getStatus())) {
                degradedCount++;
            } else {
                failedCount++;
            }
        }

        String overallStatus = (failedCount > 0)
                ? "CRITICAL"
                : (degradedCount > 0 ? "DEGRADED" : "HEALTHY");

        return PipelinesSummaryDTO.builder()
                .overallStatus(overallStatus)
                .totalPipelines(knownPipelines.size())
                .healthyCount(healthyCount)
                .degradedCount(degradedCount)
                .failedCount(failedCount)
                .pipelines(pipelineSummaries)
                .timestamp(now)
                .build();
    }

    /**
     * Retrieves recent execution logs for a specific pipeline.
     *
     * @param pipelineName Name of the pipeline.
     * @return List of recent logs.
     */
    @Transactional(readOnly = true)
    public List<PipelineExecutionLog> getPipelineHistory(String pipelineName) {
        return pipelineLogRepository.findTop10ByPipelineNameOrderByStartTimeDesc(pipelineName);
    }

    private PipelineHealthDTO getPipelineHealth(String pipelineName, LocalDateTime now) {
        Optional<PipelineExecutionLog> lastLogOpt =
                pipelineLogRepository.findTopByPipelineNameOrderByStartTimeDesc(pipelineName);

        long totalRuns = pipelineLogRepository.countByPipelineName(pipelineName);
        long successfulRuns = pipelineLogRepository.countByPipelineNameAndExecutionStatus(
                pipelineName, PipelineExecutionLog.PipelineStatus.SUCCESS)
                + pipelineLogRepository.countByPipelineNameAndExecutionStatus(
                pipelineName, PipelineExecutionLog.PipelineStatus.DEGRADED);
        long failedRuns = pipelineLogRepository.countByPipelineNameAndExecutionStatus(
                pipelineName, PipelineExecutionLog.PipelineStatus.FAILED);

        double successRate = (totalRuns > 0)
                ? ((double) successfulRuns / totalRuns) * 100.0
                : 100.0;

        if (lastLogOpt.isEmpty()) {
            return PipelineHealthDTO.builder()
                    .pipelineName(pipelineName)
                    .status("HEALTHY")
                    .lastRunTime(null)
                    .lastDurationMs(null)
                    .successRate(100.0)
                    .totalRuns(0)
                    .successfulRuns(0)
                    .failedRuns(0)
                    .recordsProcessed(0)
                    .freshnessLagSeconds(0L)
                    .errorMessage(null)
                    .build();
        }

        PipelineExecutionLog last = lastLogOpt.get();
        Long lagSeconds = null;
        if (last.getFreshnessTimestamp() != null) {
            lagSeconds = Math.max(0, Duration.between(last.getFreshnessTimestamp(), now).getSeconds());
        }

        String status = "HEALTHY";
        if (last.getExecutionStatus() == PipelineExecutionLog.PipelineStatus.FAILED) {
            status = "FAILED";
        } else if (last.getExecutionStatus() == PipelineExecutionLog.PipelineStatus.DEGRADED
                || (lagSeconds != null && lagSeconds > FRESHNESS_LAG_WARNING_SECONDS)) {
            status = "DEGRADED";
        }

        return PipelineHealthDTO.builder()
                .pipelineName(pipelineName)
                .status(status)
                .lastRunTime(last.getStartTime())
                .lastDurationMs(last.getDurationMs())
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .totalRuns(totalRuns)
                .successfulRuns(successfulRuns)
                .failedRuns(failedRuns)
                .recordsProcessed(last.getRecordsProcessed() != null ? last.getRecordsProcessed() : 0)
                .freshnessLagSeconds(lagSeconds)
                .errorMessage(last.getErrorMessage())
                .build();
    }
}
