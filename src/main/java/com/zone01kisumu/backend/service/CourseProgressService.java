package com.zone01kisumu.backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use Spring's Transactional
import java.math.BigDecimal;

import com.zone01kisumu.backend.dto.CourseProgressDTOs;
import com.zone01kisumu.backend.dto.StudentAttendanceDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.CourseProgress;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.StudentAttendance;
import com.zone01kisumu.backend.repository.CourseTopicRepository;
import com.zone01kisumu.backend.repository.CourseProgressRepository;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.CourseLessonRepository;
import com.zone01kisumu.backend.repository.StudentAttendanceRepository;
import com.zone01kisumu.backend.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // For logging

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j // Lombok for logger
public class CourseProgressService {

    private final CourseLessonRepository courseLessonRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseTopicRepository courseTopicRepository;

    private static final String STUDENT_NOTFOUND = "Student not found with ID: ";

    /**
     * Marks attendance for a student for a specific scheduled course Lesson.
     * This method can be called manually by a teacher or dynamically by a system.
     * After marking attendance, it triggers an update to the student's
     * CourseProgress.
     *
     * @param dto The StudentAttendanceDTO containing attendance details.
     * @return The created StudentAttendanceDTO.
     */
    public StudentAttendanceDTO markAttendance(StudentAttendanceDTO dto) {
        // Fetch related entities
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        STUDENT_NOTFOUND + dto.getStudentId()));
        CourseLesson courseLesson = courseLessonRepository.findById(dto.getCourseLessonId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course Lesson not found with ID: " + dto.getCourseLessonId()));

        // Check if attendance already exists for this student and Lesson
        Optional<StudentAttendance> existingAttendance = studentAttendanceRepository
                .findByStudentAndCourseLesson(student, courseLesson);

        StudentAttendance attendance;
        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
            // Update existing attendance if needed (e.g., status change)
            attendance.setAttendanceStatus(StudentAttendance.AttendanceStatus.valueOf(dto.getAttendanceStatus()));
            attendance.setMarkedBy(StudentAttendance.MarkedBy.valueOf(dto.getMarkedBy()));
            log.warn("Updating existing attendance record for student in Lesson", student.getId(),
                    courseLesson.getId());
        } else {
            // Create new attendance record
            attendance = new StudentAttendance();
            attendance.setStudent(student);
            attendance.setCourseLesson(courseLesson);
            attendance.setAttendanceStatus(StudentAttendance.AttendanceStatus.valueOf(dto.getAttendanceStatus()));
            attendance.setMarkedBy(StudentAttendance.MarkedBy.valueOf(dto.getMarkedBy()));
            log.info("Creating new attendance record for student {} in Lesson {}.", student.getId(),
                    courseLesson.getId());
        }

        StudentAttendance savedAttendance = studentAttendanceRepository.save(attendance);

        // After marking attendance, update the student's course progress for the
        // relevant Topic
        updateCourseProgressForTopic(student, courseLesson.getTopic());

        return mapStudentAttendanceToDTO(savedAttendance);
    }

    /**
     * Updates a student's CourseProgress for a specific CourseTopic.
     * This method is called automatically after attendance is marked.
     * It recalculates the completed Lessons count and progress percentage for that
     * Topic.
     *
     * @param student The student whose progress needs to be updated.
     * @param Topic The course Topic for which progress needs to be updated.
     */
    private void updateCourseProgressForTopic(Student student, CourseTopic Topic) {
        // Find existing CourseProgress or create a new one
        CourseProgress courseProgress = courseProgressRepository
                .findByStudentAndTopic(student, Topic)
                .orElseGet(() -> {
                    CourseProgress newProgress = new CourseProgress();
                    newProgress.setStudent(student);
                    newProgress.setTopic(Topic);
                    newProgress.setCourse(Topic.getCourse()); // Link to the course via Topic
                    newProgress.setStatus(CourseProgress.Status.NOT_STARTED);
                    return newProgress;
                });

        // Count completed lessons for this student and Topic
        // A Lesson is considered 'completed' if the student was PRESENT for it.
        Long completedCount = studentAttendanceRepository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                student, Topic, StudentAttendance.AttendanceStatus.PRESENT);

        courseProgress.setCompletedLessonsCount(completedCount.intValue());

        // Get the total expected lessons for this Topic
        Integer totalExpectedLessons = Topic.getTotalExpectedLessons();
        if (totalExpectedLessons == null || totalExpectedLessons <= 0) {
            log.warn("CourseTopic has invalid totalExpectedLessons:", Topic.getId(), totalExpectedLessons);
            totalExpectedLessons = 1; // Prevent division by zero
        }

        // Calculate progress percentage
        double progressPercentage = (double) completedCount / totalExpectedLessons * 100.0;
        courseProgress.setProgressPercentage(
                BigDecimal.valueOf(Math.min(100.0, progressPercentage))); // Cap at 100%

        // Determine overall status for the Topic
        if (courseProgress.getCompletedLessonsCount() >= totalExpectedLessons) {
            courseProgress.setStatus(CourseProgress.Status.COMPLETED);
        } else if (courseProgress.getCompletedLessonsCount() > 0) {
            courseProgress.setStatus(CourseProgress.Status.IN_PROGRESS);
        } else {
            courseProgress.setStatus(CourseProgress.Status.NOT_STARTED);
        }

        courseProgressRepository.save(courseProgress);
        log.info(
                "Updated CourseProgress for student in Topic : lessons completed, %progress, Status:",
                student.getId(), Topic.getId(), completedCount, totalExpectedLessons,
                String.format("%.2f", courseProgress.getProgressPercentage()), courseProgress.getStatus());
    }

    /**
     * Retrieves a student's progress for a specific course Topic.
     *
     * @param studentId The ID of the student.
     * @param TopicId The ID of the course Topic.
     * @return CourseProgressDTO for the specified student and Topic, or null if
     *         not found.
     */
    public CourseProgressDTOs getStudentProgressForTopic(Long studentId, Long TopicId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(STUDENT_NOTFOUND + studentId));
        CourseTopic Topic = courseTopicRepository.findById(TopicId)
                .orElseThrow(() -> new IllegalArgumentException("Course Topic not found with ID: " + TopicId));

        Optional<CourseProgress> progress = courseProgressRepository.findByStudentAndTopic(student, Topic);
        return progress.map(this::mapCourseProgressToDTO).orElse(null);
    }

    /**
     * Retrieves the overall progress for a student across all Topics in a
     * specific course.
     * This is an aggregation of individual Topic progress.
     *
     * @param studentId The ID of the student.
     * @param courseId  The ID of the course.
     * @return A calculated overall progress percentage for the course.
     */
    public Double getOverallCourseProgress(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(STUDENT_NOTFOUND + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        // Get all Topics for the course
        long totalTopicsInCourse = courseTopicRepository.countByCourse(course);
        if (totalTopicsInCourse == 0) {
            return 0.0; // No Topics, no progress
        }

        /**
         * Sum up the progress percentages for each Topic for this student
         * This approach assumes each Topic contributes equally to overall course
         * progress.
         * A more complex weighting could be implemented if needed.
         */
        Double sumOfTopicProgressPercentages = courseProgressRepository
                .findProgressPercentagesByStudentAndCourse(student, course)
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // Calculate average progress across all Topics
        double overallProgress = sumOfTopicProgressPercentages / totalTopicsInCourse;

        log.info("Overall progress for student in course %", studentId, courseId,
                String.format("%.2f", overallProgress));
        return Math.min(100.0, overallProgress); // Cap at 100%
    }

    // --- Mapper Methods ---
    private StudentAttendanceDTO mapStudentAttendanceToDTO(StudentAttendance attendance) {
        return StudentAttendanceDTO.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .courseLessonId(attendance.getCourseLesson().getId())
                .attendanceStatus(attendance.getAttendanceStatus().name())
                .markedBy(attendance.getMarkedBy().name())
                .build();
    }

    private CourseProgressDTOs mapCourseProgressToDTO(CourseProgress progress) {
        return CourseProgressDTOs.builder()
                .id(progress.getId())
                .studentId(progress.getStudent().getId())
                .topicId(progress.getTopic().getId())
                .courseId(progress.getCourse().getId())
                .topicTitle(progress.getTopic().getTitle()) // Include Topic title
                .completedLessonsCount(progress.getCompletedLessonsCount())
                .totalExpectedLessons(progress.getTopic().getTotalExpectedLessons()) // Include total expected
                .progressPercentage(progress.getProgressPercentage() != null
                ? progress.getProgressPercentage().doubleValue()
                : null)
                .status(progress.getStatus().name())
                .lastUpdated(progress.getLastUpdated())
                .build();
    }
}
