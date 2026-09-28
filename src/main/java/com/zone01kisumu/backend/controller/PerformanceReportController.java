package com.zone01kisumu.backend.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.dto.PerformanceReportDTOs.ClassPerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.CoursePerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.PerformanceReportPageResponse;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentPerformanceSummaryDTO;
import com.zone01kisumu.backend.model.PerformanceSummary;
import com.zone01kisumu.backend.service.PerformanceReportService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for generating and retrieving Performance Summary Reports
 * for classes, individual students, and entire courses.
 */
@RestController
@RequestMapping("/api/reports/performance/summary")
@RequiredArgsConstructor
public class PerformanceReportController {

    private final PerformanceReportService performanceReportService;

    /**
     * Retrieves a performance summary for a specific class/topic.
     *
     * @param classId   ID of the class/topic.
     * @param startDate Optional start date filter.
     * @param endDate   Optional end date filter.
     * @return 200 OK with ClassPerformanceSummaryDTO, or 404 if not found.
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<ClassPerformanceSummaryDTO> getClassPerformanceSummary(
            @PathVariable Long classId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        try {
            ClassPerformanceSummaryDTO report =
                    performanceReportService.getClassPerformanceSummary(classId, startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a performance summary for a specific student across all enrolled courses.
     *
     * @param studentId ID of the student.
     * @param startDate Optional start date filter.
     * @param endDate   Optional end date filter.
     * @return 200 OK with StudentPerformanceSummaryDTO, or 404 if not found.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentPerformanceSummaryDTO> getStudentPerformanceSummary(
            @PathVariable Long studentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        try {
            StudentPerformanceSummaryDTO report =
                    performanceReportService.getStudentPerformanceSummary(studentId, startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a performance summary for a specific course.
     *
     * @param courseId  ID of the course.
     * @param startDate Optional start date filter.
     * @param endDate   Optional end date filter.
     * @return 200 OK with CoursePerformanceSummaryDTO, or 404 if not found.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<CoursePerformanceSummaryDTO> getCoursePerformanceSummary(
            @PathVariable Long courseId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        try {
            CoursePerformanceSummaryDTO report =
                    performanceReportService.getCoursePerformanceSummary(courseId, startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves historical performance summary reports with pagination and filtering.
     *
     * @param reportType Optional report type filter (CLASS, STUDENT, COURSE).
     * @param entityId   Optional entity ID filter.
     * @param page       0-based page number (default 0).
     * @param size       Page size (default 20).
     * @return Paginated list of PerformanceSummary snapshots.
     */
    @GetMapping("/history")
    public ResponseEntity<PerformanceReportPageResponse<PerformanceSummary>> getHistoricalReports(
            @RequestParam(required = false) PerformanceSummary.ReportType reportType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateGenerated"));
            PerformanceReportPageResponse<PerformanceSummary> response =
                    performanceReportService.getHistoricalSummaries(reportType, entityId, pageRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
