package com.zone01kisumu.backend.reportTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.PerformanceReportDTOs.ClassPerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.CoursePerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.PerformanceReportPageResponse;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentClassMetricDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentCourseMetricDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentPerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.TopicPerformanceMetricDTO;

class PerformanceReportDTOsTest {

    @Test
    void testClassPerformanceSummaryDTO() {
        LocalDateTime now = LocalDateTime.now();
        StudentClassMetricDTO studentMetric = StudentClassMetricDTO.builder()
                .studentId(1L)
                .studentName("Alice Doe")
                .email("alice@test.com")
                .completedLessons(4)
                .completionRate(100.0)
                .attendanceRate(100.0)
                .status("COMPLETED")
                .build();

        ClassPerformanceSummaryDTO dto = ClassPerformanceSummaryDTO.builder()
                .classId(10L)
                .className("Topic 1")
                .courseId(20L)
                .courseTitle("Java 101")
                .totalStudents(1)
                .activeStudents(1)
                .totalLessons(4)
                .completedLessonsTotal(4)
                .averageCompletionRate(100.0)
                .averageAttendanceRate(100.0)
                .engagementScore(100.0)
                .studentSummaries(List.of(studentMetric))
                .dateGenerated(now)
                .build();

        assertEquals(10L, dto.getClassId());
        assertEquals("Topic 1", dto.getClassName());
        assertEquals(20L, dto.getCourseId());
        assertEquals("Java 101", dto.getCourseTitle());
        assertEquals(1, dto.getTotalStudents());
        assertEquals(1, dto.getActiveStudents());
        assertEquals(4, dto.getTotalLessons());
        assertEquals(4, dto.getCompletedLessonsTotal());
        assertEquals(100.0, dto.getAverageCompletionRate());
        assertEquals(100.0, dto.getAverageAttendanceRate());
        assertEquals(100.0, dto.getEngagementScore());
        assertEquals(1, dto.getStudentSummaries().size());
        assertEquals(now, dto.getDateGenerated());
        assertNotNull(dto.toString());
    }

    @Test
    void testStudentPerformanceSummaryDTO() {
        LocalDateTime now = LocalDateTime.now();
        StudentCourseMetricDTO courseMetric = StudentCourseMetricDTO.builder()
                .courseId(100L)
                .courseTitle("Spring Boot")
                .totalLessons(10)
                .completedLessons(8)
                .completionRate(80.0)
                .attendanceRate(90.0)
                .status("IN_PROGRESS")
                .build();

        StudentPerformanceSummaryDTO dto = StudentPerformanceSummaryDTO.builder()
                .studentId(5L)
                .studentName("Bob Smith")
                .email("bob@test.com")
                .totalCoursesEnrolled(1)
                .completedCoursesCount(0)
                .inProgressCoursesCount(1)
                .overallCompletionRate(80.0)
                .totalLessonsCompleted(8)
                .attendanceRate(90.0)
                .engagementScore(84.0)
                .courseBreakdowns(List.of(courseMetric))
                .dateGenerated(now)
                .build();

        assertEquals(5L, dto.getStudentId());
        assertEquals("Bob Smith", dto.getStudentName());
        assertEquals("bob@test.com", dto.getEmail());
        assertEquals(1, dto.getTotalCoursesEnrolled());
        assertEquals(0, dto.getCompletedCoursesCount());
        assertEquals(1, dto.getInProgressCoursesCount());
        assertEquals(80.0, dto.getOverallCompletionRate());
        assertEquals(8, dto.getTotalLessonsCompleted());
        assertEquals(90.0, dto.getAttendanceRate());
        assertEquals(84.0, dto.getEngagementScore());
        assertEquals(1, dto.getCourseBreakdowns().size());
        assertNotNull(dto.toString());
    }

    @Test
    void testCoursePerformanceSummaryDTO() {
        LocalDateTime now = LocalDateTime.now();
        TopicPerformanceMetricDTO topicMetric = TopicPerformanceMetricDTO.builder()
                .topicId(50L)
                .topicTitle("Basics")
                .expectedLessons(5)
                .completedLessonsTotal(10)
                .completionRate(100.0)
                .build();

        CoursePerformanceSummaryDTO dto = CoursePerformanceSummaryDTO.builder()
                .courseId(200L)
                .courseTitle("Data Structures")
                .teacherId(3L)
                .totalEnrolledStudents(2)
                .totalLessons(10)
                .totalTopics(2)
                .averageCompletionRate(85.0)
                .overallAttendanceRate(90.0)
                .engagementScore(87.0)
                .studentsCompletedCount(1)
                .studentsInProgressCount(1)
                .studentsNotStartedCount(0)
                .topicSummaries(List.of(topicMetric))
                .dateGenerated(now)
                .build();

        assertEquals(200L, dto.getCourseId());
        assertEquals("Data Structures", dto.getCourseTitle());
        assertEquals(3L, dto.getTeacherId());
        assertEquals(2, dto.getTotalEnrolledStudents());
        assertEquals(10, dto.getTotalLessons());
        assertEquals(2, dto.getTotalTopics());
        assertEquals(85.0, dto.getAverageCompletionRate());
        assertEquals(90.0, dto.getOverallAttendanceRate());
        assertEquals(87.0, dto.getEngagementScore());
        assertEquals(1, dto.getStudentsCompletedCount());
        assertEquals(1, dto.getStudentsInProgressCount());
        assertEquals(0, dto.getStudentsNotStartedCount());
        assertEquals(1, dto.getTopicSummaries().size());
        assertNotNull(dto.toString());
    }

    @Test
    void testPerformanceReportPageResponse() {
        PerformanceReportPageResponse<String> page = PerformanceReportPageResponse.<String>builder()
                .content(List.of("Report1", "Report2"))
                .currentPage(0)
                .pageSize(10)
                .totalElements(2L)
                .totalPages(1)
                .isLast(true)
                .build();

        assertEquals(2, page.getContent().size());
        assertEquals(0, page.getCurrentPage());
        assertEquals(10, page.getPageSize());
        assertEquals(2L, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertTrue(page.isLast());
        assertNotNull(page.toString());
    }
}
