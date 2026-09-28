package com.zone01kisumu.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CompletedLessonEntry;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CourseProgressResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonCompletionResultDTO;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonStatusResponse;
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
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service for managing student lesson completion tracking and course progress.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LessonCompletionService {

    private final StudentRepository studentRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final CourseRepository courseRepository;
    private final CourseTopicRepository courseTopicRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final CourseProgressRepository courseProgressRepository;

    private static final String STUDENT_NOT_FOUND = "Student not found with ID: ";
    private static final String LESSON_NOT_FOUND = "Lesson not found with ID: ";
    private static final String COURSE_NOT_FOUND = "Course not found with ID: ";

    /**
     * Marks a specific lesson as completed by a student.
     *
     * @param studentId ID of the student.
     * @param lessonId  ID of the lesson.
     * @param timestamp Optional completion timestamp.
     * @return Result DTO containing completion details.
     */
    public LessonCompletionResultDTO markComplete(Long studentId, Long lessonId, LocalDateTime timestamp) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(STUDENT_NOT_FOUND + studentId));

        CourseLesson lesson = courseLessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException(LESSON_NOT_FOUND + lessonId));

        Optional<LessonCompletion> existingCompletion =
                lessonCompletionRepository.findByStudentIdAndLessonId(studentId, lessonId);

        if (existingCompletion.isPresent()
                && existingCompletion.get().getStatus() == LessonCompletion.CompletionStatus.COMPLETED) {
            throw new IllegalStateException("Lesson already marked complete for this student.");
        }

        LocalDateTime completionTime = (timestamp != null) ? timestamp : LocalDateTime.now();

        LessonCompletion completion = existingCompletion.orElseGet(() -> LessonCompletion.builder()
                .studentId(studentId)
                .lessonId(lessonId)
                .courseId(lesson.getCourseId())
                .build());

        completion.setCourseId(lesson.getCourseId());
        completion.setCompletionDate(completionTime);
        completion.setStatus(LessonCompletion.CompletionStatus.COMPLETED);

        LessonCompletion savedCompletion = lessonCompletionRepository.save(completion);

        // Synchronize with StudentAttendance
        syncAttendance(student, lesson, StudentAttendance.AttendanceStatus.PRESENT);

        // Update CourseProgress for the topic
        syncTopicProgress(student, lesson);

        LoggerUtil.logInfo("Student [{}] completed lesson [{}] for course [{}]",
                studentId, lessonId, lesson.getCourseId());

        return LessonCompletionResultDTO.builder()
                .id(savedCompletion.getId())
                .studentId(studentId)
                .lessonId(lessonId)
                .courseId(lesson.getCourseId())
                .completed(true)
                .completionDate(savedCompletion.getCompletionDate())
                .status(savedCompletion.getStatus().name())
                .message("Lesson marked as completed successfully.")
                .build();
    }

    /**
     * Unmarks a lesson as completed (resets completion status).
     *
     * @param studentId ID of the student.
     * @param lessonId  ID of the lesson.
     */
    public void unmarkComplete(Long studentId, Long lessonId) {
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException(STUDENT_NOT_FOUND + studentId);
        }

        CourseLesson lesson = courseLessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException(LESSON_NOT_FOUND + lessonId));

        LessonCompletion completion = lessonCompletionRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .filter(c -> c.getStatus() == LessonCompletion.CompletionStatus.COMPLETED)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No completed record found for student " + studentId + " and lesson " + lessonId));

        lessonCompletionRepository.delete(completion);

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student != null) {
            syncAttendance(student, lesson, StudentAttendance.AttendanceStatus.ABSENT);
            syncTopicProgress(student, lesson);
        }

        LoggerUtil.logInfo("Unmarked completion for student [{}] and lesson [{}]", studentId, lessonId);
    }

    /**
     * Retrieves the completion status for a specific student and lesson.
     *
     * @param studentId ID of the student.
     * @param lessonId  ID of the lesson.
     * @return LessonStatusResponse indicating whether completed and the completion date.
     */
    @Transactional(readOnly = true)
    public LessonStatusResponse getStatus(Long studentId, Long lessonId) {
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException(STUDENT_NOT_FOUND + studentId);
        }
        if (!courseLessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException(LESSON_NOT_FOUND + lessonId);
        }

        return lessonCompletionRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .filter(c -> c.getStatus() == LessonCompletion.CompletionStatus.COMPLETED)
                .map(c -> LessonStatusResponse.builder()
                        .completed(true)
                        .completionDate(c.getCompletionDate())
                        .build())
                .orElse(LessonStatusResponse.builder()
                        .completed(false)
                        .completionDate(null)
                        .build());
    }

    /**
     * Retrieves all completed lessons for a student within a specific course.
     *
     * @param studentId ID of the student.
     * @param courseId  ID of the course.
     * @return List of completed lesson entries.
     */
    @Transactional(readOnly = true)
    public List<CompletedLessonEntry> getCompletedLessons(Long studentId, Long courseId) {
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException(STUDENT_NOT_FOUND + studentId);
        }
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException(COURSE_NOT_FOUND + courseId);
        }

        return lessonCompletionRepository.findByStudentIdAndCourseId(studentId, courseId).stream()
                .filter(c -> c.getStatus() == LessonCompletion.CompletionStatus.COMPLETED)
                .map(c -> {
                    String lessonName = null;
                    Long topicId = null;
                    Optional<CourseLesson> lessonOpt = courseLessonRepository.findById(c.getLessonId());
                    if (lessonOpt.isPresent()) {
                        lessonName = lessonOpt.get().getLessonName();
                        topicId = lessonOpt.get().getTopicId();
                    }
                    return CompletedLessonEntry.builder()
                            .lessonId(c.getLessonId())
                            .lessonName(lessonName)
                            .courseId(c.getCourseId())
                            .topicId(topicId)
                            .completionDate(c.getCompletionDate())
                            .status(c.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculates the course completion progress for a student.
     *
     * @param studentId ID of the student.
     * @param courseId  ID of the course.
     * @return CourseProgressResponse with total, completed, and percentage.
     */
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(Long studentId, Long courseId) {
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException(STUDENT_NOT_FOUND + studentId);
        }
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException(COURSE_NOT_FOUND + courseId);
        }

        long totalLessons = courseLessonRepository.countByCourseId(courseId);
        long completedLessons = lessonCompletionRepository.countByStudentIdAndCourseId(studentId, courseId);

        double percentage = (totalLessons <= 0)
                ? 0.0
                : Math.min(100.0, ((double) completedLessons / totalLessons) * 100.0);

        double roundedPercentage = Math.round(percentage * 100.0) / 100.0;

        return CourseProgressResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .percentageCompleted(roundedPercentage)
                .build();
    }

    private void syncAttendance(Student student, CourseLesson lesson, StudentAttendance.AttendanceStatus status) {
        try {
            Optional<StudentAttendance> attendanceOpt =
                    studentAttendanceRepository.findByStudentAndCourseLesson(student, lesson);

            StudentAttendance attendance = attendanceOpt.orElseGet(StudentAttendance::new);
            attendance.setStudent(student);
            attendance.setCourseLesson(lesson);
            attendance.setAttendanceStatus(status);
            attendance.setMarkedBy(StudentAttendance.MarkedBy.SYSTEM);

            studentAttendanceRepository.save(attendance);
        } catch (Exception e) {
            LoggerUtil.logError("Failed to sync attendance for student [{}] and lesson [{}]: {}",
                    student.getId(), lesson.getId(), e.getMessage());
        }
    }

    private void syncTopicProgress(Student student, CourseLesson lesson) {
        try {
            CourseTopic topic = lesson.getTopic();
            if (topic == null && lesson.getTopicId() != null) {
                topic = courseTopicRepository.findById(lesson.getTopicId()).orElse(null);
            }
            if (topic == null) {
                return;
            }

            final CourseTopic topicRef = topic;
            CourseProgress progress = courseProgressRepository
                    .findByStudentAndTopic(student, topicRef)
                    .orElseGet(() -> {
                        CourseProgress cp = new CourseProgress();
                        cp.setStudent(student);
                        cp.setTopic(topicRef);
                        cp.setCourse(topicRef.getCourse());
                        cp.setStatus(CourseProgress.Status.NOT_STARTED);
                        return cp;
                    });

            long completedCount = studentAttendanceRepository
                    .countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                            student, topicRef, StudentAttendance.AttendanceStatus.PRESENT);

            progress.setCompletedLessonsCount((int) completedCount);

            int expected = (topicRef.getTotalExpectedLessons() != null && topicRef.getTotalExpectedLessons() > 0)
                    ? topicRef.getTotalExpectedLessons() : 1;

            double pct = ((double) completedCount / expected) * 100.0;
            progress.setProgressPercentage(BigDecimal.valueOf(Math.min(100.0, pct)));

            if (completedCount >= expected) {
                progress.setStatus(CourseProgress.Status.COMPLETED);
            } else if (completedCount > 0) {
                progress.setStatus(CourseProgress.Status.IN_PROGRESS);
            } else {
                progress.setStatus(CourseProgress.Status.NOT_STARTED);
            }

            courseProgressRepository.save(progress);
        } catch (Exception e) {
            LoggerUtil.logError("Failed to sync course topic progress for student [{}]: {}",
                    student.getId(), e.getMessage());
        }
    }
}
