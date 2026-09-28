package com.zone01kisumu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.StudentAttendance;

@Repository
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, Long> {
    Optional<StudentAttendance> findByStudentAndCourseLesson(Student student, CourseLesson courseLesson);

    // Custom query to count completed lessons for a student in a specific Topic
    Long countByStudentAndCourseLesson_TopicAndAttendanceStatus(
            Student student, CourseTopic topic, StudentAttendance.AttendanceStatus status);

    List<StudentAttendance> findByStudent(Student student);

    List<StudentAttendance> findByStudentAndCourseLesson_Course(Student student, Course course);

    long countByCourseLesson_CourseAndAttendanceStatus(
            Course course, StudentAttendance.AttendanceStatus status);

    long countByCourseLesson_Course(Course course);
}
