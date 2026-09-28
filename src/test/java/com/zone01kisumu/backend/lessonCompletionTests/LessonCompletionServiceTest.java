package com.zone01kisumu.backend.lessonCompletionTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CompletedLessonEntry;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CourseProgressResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonCompletionResultDTO;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonStatusResponse;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.CourseProgress;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.LessonCompletion;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.StudentAttendance;
import com.zone01kisumu.backend.repository.CourseLessonRepository;
import com.zone01kisumu.backend.repository.CourseProgressRepository;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.CourseTopicRepository;
import com.zone01kisumu.backend.repository.LessonCompletionRepository;
import com.zone01kisumu.backend.repository.StudentAttendanceRepository;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.service.LessonCompletionService;

@ExtendWith(MockitoExtension.class)
class LessonCompletionServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseLessonRepository courseLessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseTopicRepository courseTopicRepository;

    @Mock
    private LessonCompletionRepository lessonCompletionRepository;

    @Mock
    private StudentAttendanceRepository studentAttendanceRepository;

    @Mock
    private CourseProgressRepository courseProgressRepository;

    @InjectMocks
    private LessonCompletionService lessonCompletionService;

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
                .build();

        mockTopic = new CourseTopic();
        mockTopic.setId(100L);
        mockTopic.setTitle("Topic 1");
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
    void markComplete_success_defaultTimestamp() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(courseLessonRepository.findById(1000L)).thenReturn(Optional.of(mockLesson));
        when(lessonCompletionRepository.findByStudentIdAndLessonId(1L, 1000L)).thenReturn(Optional.empty());

        LessonCompletion saved = LessonCompletion.builder()
                .id(50L)
                .studentId(1L)
                .lessonId(1000L)
                .courseId(10L)
                .completionDate(LocalDateTime.now())
                .status(LessonCompletion.CompletionStatus.COMPLETED)
                .build();
        when(lessonCompletionRepository.save(any(LessonCompletion.class))).thenReturn(saved);

        when(studentAttendanceRepository.findByStudentAndCourseLesson(mockStudent, mockLesson))
                .thenReturn(Optional.empty());
        when(courseProgressRepository.findByStudentAndTopic(mockStudent, mockTopic))
                .thenReturn(Optional.of(new CourseProgress()));
        when(studentAttendanceRepository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                mockStudent, mockTopic, StudentAttendance.AttendanceStatus.PRESENT)).thenReturn(1L);

        LessonCompletionResultDTO result = lessonCompletionService.markComplete(1L, 1000L, null);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals(1L, result.getStudentId());
        assertEquals(1000L, result.getLessonId());
        assertEquals(10L, result.getCourseId());
        assertTrue(result.isCompleted());
        verify(lessonCompletionRepository).save(any(LessonCompletion.class));
        verify(studentAttendanceRepository).save(any(StudentAttendance.class));
    }

    @Test
    void markComplete_success_customTimestamp() {
        LocalDateTime customTime = LocalDateTime.of(2026, 9, 1, 10, 0);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(courseLessonRepository.findById(1000L)).thenReturn(Optional.of(mockLesson));
        when(lessonCompletionRepository.findByStudentIdAndLessonId(1L, 1000L)).thenReturn(Optional.empty());

        LessonCompletion saved = LessonCompletion.builder()
                .id(51L)
                .studentId(1L)
                .lessonId(1000L)
                .courseId(10L)
                .completionDate(customTime)
                .status(LessonCompletion.CompletionStatus.COMPLETED)
                .build();
        when(lessonCompletionRepository.save(any(LessonCompletion.class))).thenReturn(saved);

        LessonCompletionResultDTO result = lessonCompletionService.markComplete(1L, 1000L, customTime);

        assertNotNull(result);
        assertEquals(customTime, result.getCompletionDate());
    }

    @Test
    void markComplete_studentNotFound_throwsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonCompletionService.markComplete(99L, 1000L, null));
        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void markComplete_lessonNotFound_throwsException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(courseLessonRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonCompletionService.markComplete(1L, 999L, null));
        assertTrue(ex.getMessage().contains("Lesson not found"));
    }

    @Test
    void markComplete_alreadyCompleted_throwsIllegalStateException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(courseLessonRepository.findById(1000L)).thenReturn(Optional.of(mockLesson));

        LessonCompletion existing = LessonCompletion.builder()
                .id(1L)
                .studentId(1L)
                .lessonId(1000L)
                .status(LessonCompletion.CompletionStatus.COMPLETED)
                .build();
        when(lessonCompletionRepository.findByStudentIdAndLessonId(1L, 1000L)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> lessonCompletionService.markComplete(1L, 1000L, null));
        assertTrue(ex.getMessage().contains("already marked complete"));
    }

    @Test
    void unmarkComplete_success() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(courseLessonRepository.findById(1000L)).thenReturn(Optional.of(mockLesson));

        LessonCompletion existing = LessonCompletion.builder()
                .id(50L)
                .studentId(1L)
                .lessonId(1000L)
                .status(LessonCompletion.CompletionStatus.COMPLETED)
                .build();
        when(lessonCompletionRepository.findByStudentIdAndLessonId(1L, 1000L)).thenReturn(Optional.of(existing));

        lessonCompletionService.unmarkComplete(1L, 1000L);

        verify(lessonCompletionRepository).delete(existing);
        verify(studentAttendanceRepository).save(any(StudentAttendance.class));
    }

    @Test
    void unmarkComplete_studentNotFound_throwsException() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonCompletionService.unmarkComplete(99L, 1000L));
        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void unmarkComplete_lessonNotFound_throwsException() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseLessonRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonCompletionService.unmarkComplete(1L, 999L));
        assertTrue(ex.getMessage().contains("Lesson not found"));
    }

    @Test
    void unmarkComplete_notCompleted_throwsException() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseLessonRepository.findById(1000L)).thenReturn(Optional.of(mockLesson));
        when(lessonCompletionRepository.findByStudentIdAndLessonId(1L, 1000L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonCompletionService.unmarkComplete(1L, 1000L));
        assertTrue(ex.getMessage().contains("No completed record found"));
    }

    @Test
    void getStatus_completedTrue() {
        LocalDateTime date = LocalDateTime.of(2026, 9, 10, 8, 30);
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseLessonRepository.existsById(1000L)).thenReturn(true);

        LessonCompletion completion = LessonCompletion.builder()
                .studentId(1L)
                .lessonId(1000L)
                .completionDate(date)
                .status(LessonCompletion.CompletionStatus.COMPLETED)
                .build();
        when(lessonCompletionRepository.findByStudentIdAndLessonId(1L, 1000L)).thenReturn(Optional.of(completion));

        LessonStatusResponse resp = lessonCompletionService.getStatus(1L, 1000L);

        assertTrue(resp.isCompleted());
        assertEquals(date, resp.getCompletionDate());
    }

    @Test
    void getStatus_completedFalse() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseLessonRepository.existsById(1000L)).thenReturn(true);
        when(lessonCompletionRepository.findByStudentIdAndLessonId(1L, 1000L)).thenReturn(Optional.empty());

        LessonStatusResponse resp = lessonCompletionService.getStatus(1L, 1000L);

        assertFalse(resp.isCompleted());
        assertNull(resp.getCompletionDate());
    }

    @Test
    void getStatus_studentNotFound_throwsException() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonCompletionService.getStatus(99L, 1000L));
    }

    @Test
    void getStatus_lessonNotFound_throwsException() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseLessonRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonCompletionService.getStatus(1L, 999L));
    }

    @Test
    void getCompletedLessons_success() {
        LocalDateTime now = LocalDateTime.now();
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(10L)).thenReturn(true);

        LessonCompletion c1 = LessonCompletion.builder()
                .lessonId(1000L)
                .courseId(10L)
                .completionDate(now)
                .status(LessonCompletion.CompletionStatus.COMPLETED)
                .build();
        when(lessonCompletionRepository.findByStudentIdAndCourseId(1L, 10L)).thenReturn(List.of(c1));
        when(courseLessonRepository.findById(1000L)).thenReturn(Optional.of(mockLesson));

        List<CompletedLessonEntry> result = lessonCompletionService.getCompletedLessons(1L, 10L);

        assertEquals(1, result.size());
        assertEquals(1000L, result.get(0).getLessonId());
        assertEquals("Lesson 1: Intro", result.get(0).getLessonName());
        assertEquals(100L, result.get(0).getTopicId());
        assertEquals(now, result.get(0).getCompletionDate());
    }

    @Test
    void getCompletedLessons_studentNotFound_throwsException() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonCompletionService.getCompletedLessons(99L, 10L));
    }

    @Test
    void getCompletedLessons_courseNotFound_throwsException() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonCompletionService.getCompletedLessons(1L, 99L));
    }

    @Test
    void getCourseProgress_success() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(10L)).thenReturn(true);
        when(courseLessonRepository.countByCourseId(10L)).thenReturn(4L);
        when(lessonCompletionRepository.countByStudentIdAndCourseId(1L, 10L)).thenReturn(3L);

        CourseProgressResponse resp = lessonCompletionService.getCourseProgress(1L, 10L);

        assertEquals(1L, resp.getStudentId());
        assertEquals(10L, resp.getCourseId());
        assertEquals(4L, resp.getTotalLessons());
        assertEquals(3L, resp.getCompletedLessons());
        assertEquals(75.0, resp.getPercentageCompleted());
    }

    @Test
    void getCourseProgress_zeroTotalLessons_returnsZeroPercentage() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(10L)).thenReturn(true);
        when(courseLessonRepository.countByCourseId(10L)).thenReturn(0L);
        when(lessonCompletionRepository.countByStudentIdAndCourseId(1L, 10L)).thenReturn(0L);

        CourseProgressResponse resp = lessonCompletionService.getCourseProgress(1L, 10L);

        assertEquals(0L, resp.getTotalLessons());
        assertEquals(0L, resp.getCompletedLessons());
        assertEquals(0.0, resp.getPercentageCompleted());
    }

    @Test
    void getCourseProgress_studentNotFound_throwsException() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonCompletionService.getCourseProgress(99L, 10L));
    }

    @Test
    void getCourseProgress_courseNotFound_throwsException() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonCompletionService.getCourseProgress(1L, 99L));
    }
}
