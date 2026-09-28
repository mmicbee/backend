package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.zone01kisumu.backend.dto.MonitoringDTOs.PipelinesSummaryDTO;
import com.zone01kisumu.backend.model.MonitoringAlert;
import com.zone01kisumu.backend.model.PipelineExecutionLog;
import com.zone01kisumu.backend.repository.PipelineExecutionLogRepository;
import com.zone01kisumu.backend.service.MonitoringAlertService;
import com.zone01kisumu.backend.service.ReportPipelineMonitoringService;

@ExtendWith(MockitoExtension.class)
class ReportPipelineMonitoringServiceTest {

    @Mock
    private PipelineExecutionLogRepository pipelineLogRepository;

    @Mock
    private MonitoringAlertService alertService;

    @InjectMocks
    private ReportPipelineMonitoringService pipelineMonitoringService;

    private PipelineExecutionLog mockLog;

    @BeforeEach
    void setUp() {
        mockLog = PipelineExecutionLog.builder()
                .id(1L)
                .pipelineName(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE)
                .executionStatus(PipelineExecutionLog.PipelineStatus.SUCCESS)
                .startTime(LocalDateTime.now().minusSeconds(1))
                .endTime(LocalDateTime.now())
                .durationMs(1000L)
                .recordsProcessed(50L)
                .freshnessTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void recordPipelineExecution_successNormal() {
        when(pipelineLogRepository.save(any(PipelineExecutionLog.class))).thenReturn(mockLog);

        PipelineExecutionLog result = pipelineMonitoringService.recordPipelineExecution(
                ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE,
                1000L,
                50L,
                true,
                null
        );

        assertNotNull(result);
        assertEquals(PipelineExecutionLog.PipelineStatus.SUCCESS, result.getExecutionStatus());
        verify(pipelineLogRepository).save(any(PipelineExecutionLog.class));
    }

    @Test
    void recordPipelineExecution_successDegradedDuration_triggersWarningAlert() {
        PipelineExecutionLog degradedLog = PipelineExecutionLog.builder()
                .pipelineName(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE)
                .executionStatus(PipelineExecutionLog.PipelineStatus.DEGRADED)
                .durationMs(6000L)
                .build();
        when(pipelineLogRepository.save(any(PipelineExecutionLog.class))).thenReturn(degradedLog);

        PipelineExecutionLog result = pipelineMonitoringService.recordPipelineExecution(
                ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE,
                6000L,
                100L,
                true,
                null
        );

        assertNotNull(result);
        assertEquals(PipelineExecutionLog.PipelineStatus.DEGRADED, result.getExecutionStatus());
        verify(alertService).raiseAlert(
                eq(MonitoringAlert.AlertSeverity.WARNING),
                eq(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE),
                any(),
                any(),
                eq(6000.0),
                eq(5000.0)
        );
    }

    @Test
    void recordPipelineExecution_failed_triggersCriticalAlert() {
        PipelineExecutionLog failedLog = PipelineExecutionLog.builder()
                .pipelineName(ReportPipelineMonitoringService.COURSE_REPORT_PIPELINE)
                .executionStatus(PipelineExecutionLog.PipelineStatus.FAILED)
                .errorMessage("DB connection timeout")
                .build();
        when(pipelineLogRepository.save(any(PipelineExecutionLog.class))).thenReturn(failedLog);

        PipelineExecutionLog result = pipelineMonitoringService.recordPipelineExecution(
                ReportPipelineMonitoringService.COURSE_REPORT_PIPELINE,
                500L,
                0L,
                false,
                "DB connection timeout"
        );

        assertNotNull(result);
        assertEquals(PipelineExecutionLog.PipelineStatus.FAILED, result.getExecutionStatus());
        verify(alertService).raiseAlert(
                eq(MonitoringAlert.AlertSeverity.CRITICAL),
                eq(ReportPipelineMonitoringService.COURSE_REPORT_PIPELINE),
                any(),
                any(),
                eq(500.0),
                eq(5000.0)
        );
    }

    @Test
    void getPipelinesSummary_returnsAggregatedSummary() {
        when(pipelineLogRepository.findAll()).thenReturn(List.of(mockLog));
        when(pipelineLogRepository.findTopByPipelineNameOrderByStartTimeDesc(any())).thenReturn(Optional.of(mockLog));
        when(pipelineLogRepository.countByPipelineName(any())).thenReturn(10L);
        when(pipelineLogRepository.countByPipelineNameAndExecutionStatus(
                any(), eq(PipelineExecutionLog.PipelineStatus.SUCCESS))).thenReturn(10L);

        PipelinesSummaryDTO summary = pipelineMonitoringService.getPipelinesSummary();

        assertNotNull(summary);
        assertEquals("HEALTHY", summary.getOverallStatus());
        assertTrue(summary.getTotalPipelines() >= 4);
        assertTrue(summary.getHealthyCount() >= 4);
    }

    @Test
    void getPipelineHistory_returnsLogs() {
        when(pipelineLogRepository.findTop10ByPipelineNameOrderByStartTimeDesc(
                ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE))
                .thenReturn(List.of(mockLog));

        List<PipelineExecutionLog> history =
                pipelineMonitoringService.getPipelineHistory(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE);

        assertEquals(1, history.size());
        assertEquals(ReportPipelineMonitoringService.CLASS_REPORT_PIPELINE, history.get(0).getPipelineName());
    }
}
