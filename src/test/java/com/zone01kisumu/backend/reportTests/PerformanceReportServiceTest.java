package com.zone01kisumu.backend.reportTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.ClassPerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.CoursePerformanceSummaryDTO;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.PerformanceReportPageResponse;
import com.zone01kisumu.backend.dto.PerformanceReportDTOs.StudentPerformanceSummaryDTO;
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
import com.zone01kisumu.backend.service.PerformanceReportService;

@ExtendWith(MockitoExtension.class)
class PerformanceReportServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseTopicRepository courseTopicRepository;

    @Mock
    private CourseLessonRepository courseLessonRepository;

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Mock
    private LessonCompletionRepository lessonCompletionRepository;

    @Mock
    private StudentAttendanceRepository studentAttendanceRepository;

    @Mock
    private PerformanceSummaryRepository performanceSummaryRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PerformanceReportService performanceReportService;

    private Student mockStudent;
    private Course mockCourse;
    private CourseTopic mockTopic;
    private CourseLesson mockLesson;

    @BeforeEach
    void setUp() {
        mockStudent = Student.builder()
                .id(1L)
                .email("student@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockCourse = Course.builder()
                .id(10L)
                .title("Java Fundamentals")
                .teacherId(5L)
                .build();

        mockTopic = new CourseTopic();
        mockTopic.setId(100L);
        mockTopic.setTitle("Topic 1: OOP");
        mockTopic.setCourse(mockCourse);
        mockTopic.setTotalExpectedLessons(4);

        mockLesson = new CourseLesson();
        mockLesson.setId(1000L);
        mockLesson.setCourseId(10L);
        mockLesson.setTopicId(100L);
        mockLesson.setLessonName("Lesson 1: Intro");
        mockLesson.setTopic(mockTopic);
    }

    @Test
    void getClassPerformanceSummary_success() {
        when(courseTopicRepository.findById(100L)).thenReturn(Optional.of(mockTopic));
        when(courseLessonRepository.findByTopicId(100L)).thenReturn(List.of(mockLesson));

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .id(50L)
                .studentId(1L)
                .course(mockCourse)
                .build();
        when(courseEnrollmentRepository.findByCourseId(10L)).thenReturn(List.of(enrollment));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(studentAttendanceRepository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                mockStudent, mockTopic, StudentAttendance.AttendanceStatus.PRESENT)).thenReturn(2L);

        StudentAttendance attendance = new StudentAttendance();
        attendance.setAttendanceStatus(StudentAttendance.AttendanceStatus.PRESENT);
        when(studentAttendanceRepository.findByStudentAndCourseLesson_Course(mockStudent, mockCourse))
                .thenReturn(List.of(attendance));

        ClassPerformanceSummaryDTO result =
                performanceReportService.getClassPerformanceSummary(100L, null, null);

        assertNotNull(result);
        assertEquals(100L, result.getClassId());
        assertEquals("Topic 1: OOP", result.getClassName());
        assertEquals(10L, result.getCourseId());
        assertEquals(1, result.getTotalStudents());
        assertEquals(1, result.getActiveStudents());
        assertEquals(4, result.getTotalLessons());
        assertEquals(2, result.getCompletedLessonsTotal());
        assertEquals(50.0, result.getAverageCompletionRate());
        assertEquals(100.0, result.getAverageAttendanceRate());
        verify(performanceSummaryRepository).save(any(PerformanceSummary.class));
    }

    @Test
    void getClassPerformanceSummary_classNotFound_throwsException() {
        when(courseTopicRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> performanceReportService.getClassPerformanceSummary(999L, null, null));
        assertTrue(ex.getMessage().contains("Class/Topic not found"));
    }

    @Test
    void getStudentPerformanceSummary_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .id(50L)
                .studentId(1L)
                .course(mockCourse)
                .build();
        when(courseEnrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));
        when(courseLessonRepository.countByCourseId(10L)).thenReturn(4L);
        when(lessonCompletionRepository.countByStudentIdAndCourseId(1L, 10L)).thenReturn(2L);

        StudentAttendance att = new StudentAttendance();
        att.setAttendanceStatus(StudentAttendance.AttendanceStatus.PRESENT);
        when(studentAttendanceRepository.findByStudentAndCourseLesson_Course(mockStudent, mockCourse))
                .thenReturn(List.of(att));

        StudentPerformanceSummaryDTO result =
                performanceReportService.getStudentPerformanceSummary(1L, null, null);

        assertNotNull(result);
        assertEquals(1L, result.getStudentId());
        assertEquals("John Doe", result.getStudentName());
        assertEquals("student@test.com", result.getEmail());
        assertEquals(1, result.getTotalCoursesEnrolled());
        assertEquals(0, result.getCompletedCoursesCount());
        assertEquals(1, result.getInProgressCoursesCount());
        assertEquals(50.0, result.getOverallCompletionRate());
        assertEquals(2, result.getTotalLessonsCompleted());
        assertEquals(100.0, result.getAttendanceRate());
        assertEquals(1, result.getCourseBreakdowns().size());
        verify(performanceSummaryRepository).save(any(PerformanceSummary.class));
    }

    @Test
    void getStudentPerformanceSummary_studentNotFound_throwsException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> performanceReportService.getStudentPerformanceSummary(999L, null, null));
        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void getCoursePerformanceSummary_success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(mockCourse));

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .id(50L)
                .studentId(1L)
                .course(mockCourse)
                .build();
        when(courseEnrollmentRepository.findByCourseId(10L)).thenReturn(List.of(enrollment));
        when(courseLessonRepository.countByCourseId(10L)).thenReturn(4L);
        when(courseTopicRepository.findByCourseId(10L)).thenReturn(List.of(mockTopic));
        when(lessonCompletionRepository.countByStudentIdAndCourseId(1L, 10L)).thenReturn(4L);
        when(studentAttendanceRepository.countByCourseLesson_Course(mockCourse)).thenReturn(2L);
        when(studentAttendanceRepository.countByCourseLesson_CourseAndAttendanceStatus(
                mockCourse, StudentAttendance.AttendanceStatus.PRESENT)).thenReturn(2L);
        when(courseLessonRepository.findByTopicId(100L)).thenReturn(List.of(mockLesson));

        CoursePerformanceSummaryDTO result =
                performanceReportService.getCoursePerformanceSummary(10L, null, null);

        assertNotNull(result);
        assertEquals(10L, result.getCourseId());
        assertEquals("Java Fundamentals", result.getCourseTitle());
        assertEquals(5L, result.getTeacherId());
        assertEquals(1, result.getTotalEnrolledStudents());
        assertEquals(4, result.getTotalLessons());
        assertEquals(1, result.getTotalTopics());
        assertEquals(100.0, result.getAverageCompletionRate());
        assertEquals(100.0, result.getOverallAttendanceRate());
        assertEquals(1, result.getStudentsCompletedCount());
        assertEquals(0, result.getStudentsInProgressCount());
        assertEquals(0, result.getStudentsNotStartedCount());
        assertEquals(1, result.getTopicSummaries().size());
        verify(performanceSummaryRepository).save(any(PerformanceSummary.class));
    }

    @Test
    void getCoursePerformanceSummary_courseNotFound_throwsException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> performanceReportService.getCoursePerformanceSummary(999L, null, null));
        assertTrue(ex.getMessage().contains("Course not found"));
    }

    @Test
    void getHistoricalSummaries_returnsPagedResults() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        PerformanceSummary summary = PerformanceSummary.builder()
                .id(1L)
                .reportId("uuid-1")
                .reportType(PerformanceSummary.ReportType.COURSE)
                .entityId(10L)
                .build();
        Page<PerformanceSummary> page = new PageImpl<>(List.of(summary), pageRequest, 1);

        when(performanceSummaryRepository.findByReportTypeAndEntityId(
                eq(PerformanceSummary.ReportType.COURSE), eq(10L), eq(pageRequest))).thenReturn(page);

        PerformanceReportPageResponse<PerformanceSummary> response =
                performanceReportService.getHistoricalSummaries(PerformanceSummary.ReportType.COURSE, 10L, pageRequest);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }
}
