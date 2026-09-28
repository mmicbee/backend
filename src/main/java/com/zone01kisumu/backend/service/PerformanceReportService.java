package com.zone01kisumu.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.ClassPerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.CoursePerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.PerformanceReportPageResponse;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentClassMetricDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentCourseMetricDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentPerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.TopicPerformanceMetricDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseEnrollment;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.PerformanceSummary;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.StudentAttendance;
import com.zone01kisumu.backend.repository.CourseEnrollmentRepository;
import com.zone01kisumu.backend.repository.CourseLessonRepository;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.CourseTopicRepository;
import com.zone01kisumu.backend.repository.LessonCompletionRepository;
import com.zone01kisumu.backend.repository.PerformanceSummaryRepository;
import com.zone01kisumu.backend.repository.StudentAttendanceRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for aggregating raw learning, attendance, and completion metrics
 * to generate performance summary reports for classes, individual students, and entire courses.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PerformanceReportService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseTopicRepository courseTopicRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final PerformanceSummaryRepository performanceSummaryRepository;
    private final ObjectMapper objectMapper;

    private static final String STUDENT_NOT_FOUND = "Student not found with ID: ";
    private static final String COURSE_NOT_FOUND = "Course not found with ID: ";
    private static final String CLASS_NOT_FOUND = "Class/Topic not found with ID: ";

    /**
     * Generates a performance summary report for a specific class/topic.
     *
     * @param classId   ID of the class/topic.
     * @param startDate Optional start filter date.
     * @param endDate   Optional end filter date.
     * @return ClassPerformanceSummaryDTO containing aggregated metrics.
     */
    public ClassPerformanceSummaryDTO getClassPerformanceSummary(
            Long classId, LocalDateTime startDate, LocalDateTime endDate) {

        CourseTopic topic = courseTopicRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException(CLASS_NOT_FOUND + classId));

        Course course = topic.getCourse();
        Long courseId = (course != null) ? course.getId() : null;
        String courseTitle = (course != null) ? course.getTitle() : "Unassigned Course";

        List<CourseLesson> lessons = courseLessonRepository.findByTopicId(classId);
        int totalLessons = (topic.getTotalExpectedLessons() != null && topic.getTotalExpectedLessons() > 0)
                ? topic.getTotalExpectedLessons()
                : lessons.size();

        List<CourseEnrollment> enrollments = (courseId != null)
                ? courseEnrollmentRepository.findByCourseId(courseId)
                : new ArrayList<>();

        int totalStudents = enrollments.size();
        int activeStudents = 0;
        int completedLessonsTotal = 0;
        double sumCompletionRate = 0.0;
        double sumAttendanceRate = 0.0;

        List<StudentClassMetricDTO> studentMetrics = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            Long studentId = enrollment.getStudentId();
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                continue;
            }

            Long completedCountLong = studentAttendanceRepository
                    .countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                            student, topic, StudentAttendance.AttendanceStatus.PRESENT);
            int studentCompleted = (completedCountLong != null) ? completedCountLong.intValue() : 0;
            completedLessonsTotal += studentCompleted;

            double completionRate = (totalLessons > 0)
                    ? Math.min(100.0, (studentCompleted * 100.0) / totalLessons)
                    : 0.0;
            sumCompletionRate += completionRate;

            List<StudentAttendance> attendances =
                    studentAttendanceRepository.findByStudentAndCourseLesson_Course(student, course);
            long totalAtt = attendances.size();
            long presentAtt = attendances.stream()
                    .filter(a -> a.getAttendanceStatus() == StudentAttendance.AttendanceStatus.PRESENT)
                    .count();
            double attendanceRate = (totalAtt > 0) ? (presentAtt * 100.0) / totalAtt : 0.0;
            sumAttendanceRate += attendanceRate;

            if (studentCompleted > 0 || totalAtt > 0) {
                activeStudents++;
            }

            String status = (completionRate >= 100.0)
                    ? "COMPLETED"
                    : (studentCompleted > 0 ? "IN_PROGRESS" : "NOT_STARTED");

            studentMetrics.add(StudentClassMetricDTO.builder()
                    .studentId(studentId)
                    .studentName(student.getFirstName() + " " + student.getLastName())
                    .email(student.getEmail())
                    .completedLessons(studentCompleted)
                    .completionRate(round(completionRate))
                    .attendanceRate(round(attendanceRate))
                    .status(status)
                    .build());
        }

        double avgCompletion = (totalStudents > 0) ? (sumCompletionRate / totalStudents) : 0.0;
        double avgAttendance = (totalStudents > 0) ? (sumAttendanceRate / totalStudents) : 0.0;
        double engagementScore = (avgCompletion * 0.6) + (avgAttendance * 0.4);

        LocalDateTime now = LocalDateTime.now();

        ClassPerformanceSummaryDTO result = ClassPerformanceSummaryDTO.builder()
                .classId(classId)
                .className(topic.getTitle())
                .courseId(courseId)
                .courseTitle(courseTitle)
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .totalLessons(totalLessons)
                .completedLessonsTotal(completedLessonsTotal)
                .averageCompletionRate(round(avgCompletion))
                .averageAttendanceRate(round(avgAttendance))
                .engagementScore(round(engagementScore))
                .studentSummaries(studentMetrics)
                .dateGenerated(now)
                .build();

        saveSnapshot(PerformanceSummary.ReportType.CLASS, classId, topic.getTitle(),
                totalStudents, totalLessons, completedLessonsTotal,
                avgCompletion, avgAttendance, engagementScore, result, now);

        return result;
    }

    /**
     * Generates a performance summary report for a specific student across all enrolled courses.
     *
     * @param studentId ID of the student.
     * @param startDate Optional start filter date.
     * @param endDate   Optional end filter date.
     * @return StudentPerformanceSummaryDTO containing aggregated metrics.
     */
    public StudentPerformanceSummaryDTO getStudentPerformanceSummary(
            Long studentId, LocalDateTime startDate, LocalDateTime endDate) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(STUDENT_NOT_FOUND + studentId));

        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByStudentId(studentId);
        int totalCoursesEnrolled = enrollments.size();

        int completedCoursesCount = 0;
        int inProgressCoursesCount = 0;
        int totalLessonsCompleted = 0;
        double sumCompletionRate = 0.0;
        double sumAttendanceRate = 0.0;

        List<StudentCourseMetricDTO> courseBreakdowns = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            if (course == null) {
                continue;
            }

            long totalLessonsLong = courseLessonRepository.countByCourseId(course.getId());
            int totalLessons = (int) totalLessonsLong;

            long completedLong = lessonCompletionRepository
                    .countByStudentIdAndCourseId(studentId, course.getId());
            int completedLessons = (int) completedLong;
            totalLessonsCompleted += completedLessons;

            double completionRate = (totalLessons > 0)
                    ? Math.min(100.0, (completedLessons * 100.0) / totalLessons)
                    : 0.0;
            sumCompletionRate += completionRate;

            List<StudentAttendance> attendances =
                    studentAttendanceRepository.findByStudentAndCourseLesson_Course(student, course);
            long totalAtt = attendances.size();
            long presentAtt = attendances.stream()
                    .filter(a -> a.getAttendanceStatus() == StudentAttendance.AttendanceStatus.PRESENT)
                    .count();
            double attRate = (totalAtt > 0) ? (presentAtt * 100.0) / totalAtt : 0.0;
            sumAttendanceRate += attRate;

            String status = "NOT_STARTED";
            if (completionRate >= 100.0) {
                status = "COMPLETED";
                completedCoursesCount++;
            } else if (completedLessons > 0 || totalAtt > 0) {
                status = "IN_PROGRESS";
                inProgressCoursesCount++;
            }

            courseBreakdowns.add(StudentCourseMetricDTO.builder()
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .totalLessons(totalLessons)
                    .completedLessons(completedLessons)
                    .completionRate(round(completionRate))
                    .attendanceRate(round(attRate))
                    .status(status)
                    .build());
        }

        double overallCompletion = (totalCoursesEnrolled > 0)
                ? (sumCompletionRate / totalCoursesEnrolled) : 0.0;
        double overallAttendance = (totalCoursesEnrolled > 0)
                ? (sumAttendanceRate / totalCoursesEnrolled) : 0.0;
        double engagementScore = (overallCompletion * 0.6) + (overallAttendance * 0.4);

        LocalDateTime now = LocalDateTime.now();

        StudentPerformanceSummaryDTO result = StudentPerformanceSummaryDTO.builder()
                .studentId(studentId)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .email(student.getEmail())
                .totalCoursesEnrolled(totalCoursesEnrolled)
                .completedCoursesCount(completedCoursesCount)
                .inProgressCoursesCount(inProgressCoursesCount)
                .overallCompletionRate(round(overallCompletion))
                .totalLessonsCompleted(totalLessonsCompleted)
                .attendanceRate(round(overallAttendance))
                .engagementScore(round(engagementScore))
                .courseBreakdowns(courseBreakdowns)
                .dateGenerated(now)
                .build();

        saveSnapshot(PerformanceSummary.ReportType.STUDENT, studentId,
                student.getFirstName() + " " + student.getLastName(),
                1, totalLessonsCompleted, totalLessonsCompleted,
                overallCompletion, overallAttendance, engagementScore, result, now);

        return result;
    }

    /**
     * Generates a performance summary report for an entire course.
     *
     * @param courseId  ID of the course.
     * @param startDate Optional start filter date.
     * @param endDate   Optional end filter date.
     * @return CoursePerformanceSummaryDTO containing aggregated metrics.
     */
    public CoursePerformanceSummaryDTO getCoursePerformanceSummary(
            Long courseId, LocalDateTime startDate, LocalDateTime endDate) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(COURSE_NOT_FOUND + courseId));

        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByCourseId(courseId);
        int totalEnrolledStudents = enrollments.size();

        long totalLessonsLong = courseLessonRepository.countByCourseId(courseId);
        int totalLessons = (int) totalLessonsLong;

        List<CourseTopic> topics = courseTopicRepository.findByCourseId(courseId);
        int totalTopics = topics.size();

        int completedStudentsCount = 0;
        int inProgressStudentsCount = 0;
        int notStartedStudentsCount = 0;
        double sumStudentCompletion = 0.0;

        for (CourseEnrollment enrollment : enrollments) {
            Long studentId = enrollment.getStudentId();
            long completedLong = lessonCompletionRepository.countByStudentIdAndCourseId(studentId, courseId);
            double pct = (totalLessons > 0)
                    ? Math.min(100.0, (completedLong * 100.0) / totalLessons)
                    : 0.0;
            sumStudentCompletion += pct;

            if (pct >= 100.0) {
                completedStudentsCount++;
            } else if (completedLong > 0) {
                inProgressStudentsCount++;
            } else {
                notStartedStudentsCount++;
            }
        }

        double avgCompletionRate = (totalEnrolledStudents > 0)
                ? (sumStudentCompletion / totalEnrolledStudents) : 0.0;

        long totalAttCount = studentAttendanceRepository.countByCourseLesson_Course(course);
        long presentAttCount = studentAttendanceRepository
                .countByCourseLesson_CourseAndAttendanceStatus(course, StudentAttendance.AttendanceStatus.PRESENT);
        double overallAttendanceRate = (totalAttCount > 0)
                ? (presentAttCount * 100.0) / totalAttCount : 0.0;

        double engagementScore = (avgCompletionRate * 0.6) + (overallAttendanceRate * 0.4);

        List<TopicPerformanceMetricDTO> topicSummaries = topics.stream().map(topic -> {
            int exp = (topic.getTotalExpectedLessons() != null && topic.getTotalExpectedLessons() > 0)
                    ? topic.getTotalExpectedLessons() : 1;
            List<CourseLesson> topicLessons = courseLessonRepository.findByTopicId(topic.getId());
            int completedInTopic = 0;
            for (CourseLesson l : topicLessons) {
                completedInTopic += lessonCompletionRepository.findAll().stream()
                        .filter(c -> c.getLessonId().equals(l.getId()))
                        .count();
            }
            double topicPct = (totalEnrolledStudents > 0 && exp > 0)
                    ? Math.min(100.0, ((double) completedInTopic / (totalEnrolledStudents * exp)) * 100.0)
                    : 0.0;

            return TopicPerformanceMetricDTO.builder()
                    .topicId(topic.getId())
                    .topicTitle(topic.getTitle())
                    .expectedLessons(exp)
                    .completedLessonsTotal(completedInTopic)
                    .completionRate(round(topicPct))
                    .build();
        }).collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        CoursePerformanceSummaryDTO result = CoursePerformanceSummaryDTO.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .teacherId(course.getTeacherId())
                .totalEnrolledStudents(totalEnrolledStudents)
                .totalLessons(totalLessons)
                .totalTopics(totalTopics)
                .averageCompletionRate(round(avgCompletionRate))
                .overallAttendanceRate(round(overallAttendanceRate))
                .engagementScore(round(engagementScore))
                .studentsCompletedCount(completedStudentsCount)
                .studentsInProgressCount(inProgressStudentsCount)
                .studentsNotStartedCount(notStartedStudentsCount)
                .topicSummaries(topicSummaries)
                .dateGenerated(now)
                .build();

        saveSnapshot(PerformanceSummary.ReportType.COURSE, courseId, course.getTitle(),
                totalEnrolledStudents, totalLessons, 0,
                avgCompletionRate, overallAttendanceRate, engagementScore, result, now);

        return result;
    }

    /**
     * Retrieves historical summary snapshots with pagination.
     *
     * @param reportType Optional report type filter.
     * @param entityId   Optional entity ID filter.
     * @param pageable   Pagination specifications.
     * @return Paginated performance summary response.
     */
    @Transactional(readOnly = true)
    public PerformanceReportPageResponse<PerformanceSummary> getHistoricalSummaries(
            PerformanceSummary.ReportType reportType, Long entityId, Pageable pageable) {

        Page<PerformanceSummary> page;
        if (reportType != null && entityId != null) {
            page = performanceSummaryRepository.findByReportTypeAndEntityId(reportType, entityId, pageable);
        } else if (reportType != null) {
            page = performanceSummaryRepository.findByReportType(reportType, pageable);
        } else {
            page = performanceSummaryRepository.findAll(pageable);
        }

        return PerformanceReportPageResponse.<PerformanceSummary>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }

    private void saveSnapshot(
            PerformanceSummary.ReportType type, Long entityId, String name,
            Integer students, Integer totalLessons, Integer completedLessons,
            Double completionRate, Double attendanceRate, Double engagementScore,
            Object details, LocalDateTime dateGenerated) {

        try {
            String json = (details != null) ? objectMapper.writeValueAsString(details) : null;
            PerformanceSummary summary = PerformanceSummary.builder()
                    .reportId(UUID.randomUUID().toString())
                    .reportType(type)
                    .entityId(entityId)
                    .entityName(name)
                    .dateGenerated(dateGenerated)
                    .totalStudents(students)
                    .totalLessons(totalLessons)
                    .completedLessons(completedLessons)
                    .completionRate(round(completionRate))
                    .attendanceRate(round(attendanceRate))
                    .engagementScore(round(engagementScore))
                    .metricsJson(json)
                    .build();

            performanceSummaryRepository.save(summary);
        } catch (Exception e) {
            LoggerUtil.logError("Failed to persist performance summary snapshot for {} [{}]: {}",
                    type, entityId, e.getMessage());
        }
    }

    private double round(Double value) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
