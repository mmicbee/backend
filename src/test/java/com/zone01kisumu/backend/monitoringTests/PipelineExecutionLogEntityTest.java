package com.zone01kisumu.backend.monitoringTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.PipelineExecutionLog;

class PipelineExecutionLogEntityTest {

    @Test
    void testBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        PipelineExecutionLog log = PipelineExecutionLog.builder()
                .id(1L)
                .pipelineName("REPORT_PIPELINE")
                .executionStatus(PipelineExecutionLog.PipelineStatus.SUCCESS)
                .startTime(now.minusSeconds(2))
                .endTime(now)
                .durationMs(2000L)
                .recordsProcessed(150L)
                .errorMessage(null)
                .freshnessTimestamp(now)
                .createdAt(now)
                .build();

        assertEquals(1L, log.getId());
        assertEquals("REPORT_PIPELINE", log.getPipelineName());
        assertEquals(PipelineExecutionLog.PipelineStatus.SUCCESS, log.getExecutionStatus());
        assertEquals(2000L, log.getDurationMs());
        assertEquals(150L, log.getRecordsProcessed());
        assertEquals(now, log.getFreshnessTimestamp());
        assertNotNull(log.toString());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        PipelineExecutionLog log = new PipelineExecutionLog();
        log.setId(2L);
        log.setPipelineName("BACKUP_PIPELINE");
        log.setExecutionStatus(PipelineExecutionLog.PipelineStatus.FAILED);
        log.setErrorMessage("Disk error");

        assertEquals(2L, log.getId());
        assertEquals("BACKUP_PIPELINE", log.getPipelineName());
        assertEquals(PipelineExecutionLog.PipelineStatus.FAILED, log.getExecutionStatus());
        assertEquals("Disk error", log.getErrorMessage());
    }

    @Test
    void testPipelineStatusEnum() {
        assertEquals(PipelineExecutionLog.PipelineStatus.SUCCESS,
                PipelineExecutionLog.PipelineStatus.valueOf("SUCCESS"));
        assertEquals(PipelineExecutionLog.PipelineStatus.FAILED,
                PipelineExecutionLog.PipelineStatus.valueOf("FAILED"));
        assertEquals(PipelineExecutionLog.PipelineStatus.RUNNING,
                PipelineExecutionLog.PipelineStatus.valueOf("RUNNING"));
        assertEquals(PipelineExecutionLog.PipelineStatus.DEGRADED,
                PipelineExecutionLog.PipelineStatus.valueOf("DEGRADED"));
        assertTrue(PipelineExecutionLog.PipelineStatus.values().length >= 4);
    }
}
