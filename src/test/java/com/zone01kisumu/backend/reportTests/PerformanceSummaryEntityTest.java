package com.zone01kisumu.backend.reportTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.PerformanceSummary;

class PerformanceSummaryEntityTest {

    @Test
    void testPerformanceSummaryBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        PerformanceSummary summary = PerformanceSummary.builder()
                .id(1L)
                .reportId("uuid-123")
                .reportType(PerformanceSummary.ReportType.COURSE)
                .entityId(10L)
                .entityName("Java 101")
                .dateGenerated(now)
                .totalStudents(50)
                .totalLessons(20)
                .completedLessons(15)
                .completionRate(75.0)
                .attendanceRate(85.0)
                .engagementScore(79.0)
                .metricsJson("{\"key\":\"val\"}")
                .createdAt(now)
                .build();

        assertEquals(1L, summary.getId());
        assertEquals("uuid-123", summary.getReportId());
        assertEquals(PerformanceSummary.ReportType.COURSE, summary.getReportType());
        assertEquals(10L, summary.getEntityId());
        assertEquals("Java 101", summary.getEntityName());
        assertEquals(now, summary.getDateGenerated());
        assertEquals(50, summary.getTotalStudents());
        assertEquals(20, summary.getTotalLessons());
        assertEquals(15, summary.getCompletedLessons());
        assertEquals(75.0, summary.getCompletionRate());
        assertEquals(85.0, summary.getAttendanceRate());
        assertEquals(79.0, summary.getEngagementScore());
        assertEquals("{\"key\":\"val\"}", summary.getMetricsJson());
        assertEquals(now, summary.getCreatedAt());
        assertNotNull(summary.toString());
    }

    @Test
    void testPerformanceSummaryNoArgsConstructorAndSetters() {
        PerformanceSummary summary = new PerformanceSummary();
        summary.setId(2L);
        summary.setReportId("uuid-456");
        summary.setReportType(PerformanceSummary.ReportType.STUDENT);
        summary.setEntityId(5L);
        summary.setCompletionRate(90.0);

        assertEquals(2L, summary.getId());
        assertEquals("uuid-456", summary.getReportId());
        assertEquals(PerformanceSummary.ReportType.STUDENT, summary.getReportType());
        assertEquals(5L, summary.getEntityId());
        assertEquals(90.0, summary.getCompletionRate());
    }

    @Test
    void testReportTypeEnum() {
        assertEquals(PerformanceSummary.ReportType.CLASS, PerformanceSummary.ReportType.valueOf("CLASS"));
        assertEquals(PerformanceSummary.ReportType.STUDENT, PerformanceSummary.ReportType.valueOf("STUDENT"));
        assertEquals(PerformanceSummary.ReportType.COURSE, PerformanceSummary.ReportType.valueOf("COURSE"));
        assertTrue(PerformanceSummary.ReportType.values().length >= 3);
    }
}
