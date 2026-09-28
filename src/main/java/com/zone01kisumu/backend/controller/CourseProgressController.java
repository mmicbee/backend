package com.zone01kisumu.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zone01kisumu.backend.dto.CourseProgressDTOs;
import com.zone01kisumu.backend.dto.StudentAttendanceDTO;
import com.zone01kisumu.backend.service.CourseProgressService;

import lombok.RequiredArgsConstructor;

/**
 * Controller for handling course progress related operations such as
 * marking attendance and retrieving student progress.
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class CourseProgressController {

    private final CourseProgressService courseProgressService;

    /**
     * Marks a student's attendance for a lession.
     *
     * @param dto Attendance data for the student.
     * @return The saved attendance record.
     */
    @PostMapping("/mark-attendance")
    public ResponseEntity<StudentAttendanceDTO> markAttendance(@RequestBody StudentAttendanceDTO dto) {
        try {
            StudentAttendanceDTO attendance = courseProgressService.markAttendance(dto);
            return ResponseEntity.ok(attendance);
        } catch (IllegalArgumentException e) {
            // Handle known business errors like invalid student/lession IDs
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Catch-all for any unexpected issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Retrieves a student's progress for a specific course topic.
     *
     * @param studentId ID of the student.
     * @param topicId ID of the course topic.
     * @return Progress details of the student for the given topic.
     */
    @GetMapping("/student-topic-progress")
    public ResponseEntity<CourseProgressDTOs> getStudentProgressForTopic(
            @RequestParam Long studentId,
            @RequestParam Long topicId) {
        try {
            CourseProgressDTOs progress = courseProgressService.getStudentProgressForTopic(studentId, topicId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Retrieves a student's overall progress for a course.
     *
     * @param studentId ID of the student.
     * @param courseId  ID of the course.
     * @return A double value representing percentage of completion.
     */
    @GetMapping("/student-overall-progress")
    public ResponseEntity<Double> getOverallProgress(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        try {
            Double progress = courseProgressService.getOverallCourseProgress(studentId, courseId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
