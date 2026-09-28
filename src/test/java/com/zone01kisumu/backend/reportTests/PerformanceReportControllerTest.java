package com.zone01kisumu.backend.reportTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.zone01kisumu.backend.controller.PerformanceReportController;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.ClassPerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.CoursePerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.PerformanceReportPageResponse;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentPerformanceSummaryDTO;
import com.zone01kisumu.backend.model.PerformanceSummary;
import com.zone01kisumu.backend.service.PerformanceReportService;

@ExtendWith(MockitoExtension.class)
class PerformanceReportControllerTest {

    @Mock
    private PerformanceReportService performanceReportService;

    @InjectMocks
    private PerformanceReportController performanceReportController;

    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2026, 9, 15, 10, 0);
    }

    @Test
    void getClassPerformanceSummary_returnsOk_onSuccess() {
        ClassPerformanceSummaryDTO dto = ClassPerformanceSummaryDTO.builder()
                .classId(1L)
                .className("Topic 1")
                .courseId(10L)
                .totalStudents(5)
                .averageCompletionRate(80.0)
                .dateGenerated(testTime)
                .build();

        when(performanceReportService.getClassPerformanceSummary(1L, null, null)).thenReturn(dto);

        ResponseEntity<ClassPerformanceSummaryDTO> response =
                performanceReportController.getClassPerformanceSummary(1L, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Topic 1", response.getBody().getClassName());
    }

    @Test
    void getClassPerformanceSummary_returnsNotFound_onIllegalArgumentException() {
        when(performanceReportService.getClassPerformanceSummary(99L, null, null))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<ClassPerformanceSummaryDTO> response =
                performanceReportController.getClassPerformanceSummary(99L, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getClassPerformanceSummary_returnsInternalError_onGenericException() {
        when(performanceReportService.getClassPerformanceSummary(1L, null, null))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<ClassPerformanceSummaryDTO> response =
                performanceReportController.getClassPerformanceSummary(1L, null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getStudentPerformanceSummary_returnsOk_onSuccess() {
        StudentPerformanceSummaryDTO dto = StudentPerformanceSummaryDTO.builder()
                .studentId(2L)
                .studentName("John Doe")
                .overallCompletionRate(75.0)
                .dateGenerated(testTime)
                .build();

        when(performanceReportService.getStudentPerformanceSummary(2L, null, null)).thenReturn(dto);

        ResponseEntity<StudentPerformanceSummaryDTO> response =
                performanceReportController.getStudentPerformanceSummary(2L, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getStudentName());
    }

    @Test
    void getStudentPerformanceSummary_returnsNotFound_onIllegalArgumentException() {
        when(performanceReportService.getStudentPerformanceSummary(99L, null, null))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<StudentPerformanceSummaryDTO> response =
                performanceReportController.getStudentPerformanceSummary(99L, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getStudentPerformanceSummary_returnsInternalError_onGenericException() {
        when(performanceReportService.getStudentPerformanceSummary(2L, null, null))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<StudentPerformanceSummaryDTO> response =
                performanceReportController.getStudentPerformanceSummary(2L, null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCoursePerformanceSummary_returnsOk_onSuccess() {
        CoursePerformanceSummaryDTO dto = CoursePerformanceSummaryDTO.builder()
                .courseId(10L)
                .courseTitle("Java 101")
                .averageCompletionRate(90.0)
                .dateGenerated(testTime)
                .build();

        when(performanceReportService.getCoursePerformanceSummary(10L, null, null)).thenReturn(dto);

        ResponseEntity<CoursePerformanceSummaryDTO> response =
                performanceReportController.getCoursePerformanceSummary(10L, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Java 101", response.getBody().getCourseTitle());
    }

    @Test
    void getCoursePerformanceSummary_returnsNotFound_onIllegalArgumentException() {
        when(performanceReportService.getCoursePerformanceSummary(99L, null, null))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<CoursePerformanceSummaryDTO> response =
                performanceReportController.getCoursePerformanceSummary(99L, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCoursePerformanceSummary_returnsInternalError_onGenericException() {
        when(performanceReportService.getCoursePerformanceSummary(10L, null, null))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<CoursePerformanceSummaryDTO> response =
                performanceReportController.getCoursePerformanceSummary(10L, null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getHistoricalReports_returnsOk_onSuccess() {
        PerformanceReportPageResponse<PerformanceSummary> pageResponse =
                PerformanceReportPageResponse.<PerformanceSummary>builder()
                        .content(List.of(new PerformanceSummary()))
                        .currentPage(0)
                        .pageSize(20)
                        .totalElements(1L)
                        .totalPages(1)
                        .isLast(true)
                        .build();

        when(performanceReportService.getHistoricalSummaries(
                eq(PerformanceSummary.ReportType.COURSE), eq(10L), any()))
                .thenReturn(pageResponse);

        ResponseEntity<PerformanceReportPageResponse<PerformanceSummary>> response =
                performanceReportController.getHistoricalReports(
                        PerformanceSummary.ReportType.COURSE, 10L, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void getHistoricalReports_returnsInternalError_onGenericException() {
        when(performanceReportService.getHistoricalSummaries(any(), any(), any()))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<PerformanceReportPageResponse<PerformanceSummary>> response =
                performanceReportController.getHistoricalReports(null, null, 0, 20);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
