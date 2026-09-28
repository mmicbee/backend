package com.zone01kisumu.backend.studentAttendanceTests;

import com.zone01kisumu.backend.dto.StudentAttendanceDTO;
import com.zone01kisumu.backend.model.*;
import com.zone01kisumu.backend.repository.*;
import com.zone01kisumu.backend.service.CourseProgressService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import com.zone01kisumu.backend.dto.CourseProgressDTOs;

// Course  Progres s Servi ce Test
@ExtendWith(MockitoExtension.class)
class CourseProgressServiceTest {

        @Mock
        private CourseLessonRepository courseLessonRepository;
        @Mock
        private CourseProgressRepository courseProgressRepository;
        @Mock
        private StudentAttendanceRepository studentAttendanceRepository;
        @Mock
        private StudentRepository studentRepository;
        @Mock
        private CourseRepository courseRepository;
        @Mock
        private CourseTopicRepository courseTopicRepository;

        @InjectMocks
        private CourseProgressService courseProgressService;

        private Student student;
        private Course course;
        private CourseTopic topic;
        private CourseLesson lesson;
        private StudentAttendance attendance;
        private CourseProgress courseProgress;

        @BeforeEach
        void setUp() {
                student = new Student();
                student.setId(1L);

                course = new Course();
                course.setId(1L);

                topic = new CourseTopic();
                topic.setId(1L);
                topic.setCourse(course);
                topic.setTitle("Java Basics");
                topic.setTotalExpectedLessons(10);

                lesson = new LiveLesson();
                lesson.setId(1L);
                lesson.setCourse(course);
                lesson.setTopic(topic);
                lesson.setLessonName("Intro");
                lesson.setLessonDate(LocalDateTime.now());
                lesson.setDuration(60);

                attendance = new StudentAttendance();
                attendance.setId(1L);
                attendance.setStudent(student);
                attendance.setCourseLesson(lesson);
                attendance.setAttendanceStatus(StudentAttendance.AttendanceStatus.PRESENT);
                attendance.setMarkedBy(StudentAttendance.MarkedBy.TEACHER);

                courseProgress = new CourseProgress();
                courseProgress.setId(1L);
                courseProgress.setStudent(student);
                courseProgress.setTopic(topic);
                courseProgress.setCourse(course);
                courseProgress.setStatus(CourseProgress.Status.NOT_STARTED);
        }

        @Test
        void testMarkAttendance_newAttendance() {
                StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                                .studentId(student.getId())
                                .courseLessonId(lesson.getId())
                                .attendanceStatus("PRESENT")
                                .markedBy("TEACHER")
                                .build();

                when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
                when(courseLessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
                when(studentAttendanceRepository.findByStudentAndCourseLesson(
                                student, lesson)).thenReturn(Optional.empty());
                when(studentAttendanceRepository.save(any())).thenReturn(attendance);
                when(studentAttendanceRepository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                                student, topic, StudentAttendance.AttendanceStatus.PRESENT))
                                .thenReturn(10L);
                when(courseProgressRepository.findByStudentAndTopic(
                                student, topic)).thenReturn(Optional.empty());

                StudentAttendanceDTO result = courseProgressService.markAttendance(dto);
                assertNotNull(result);
                assertEquals(attendance.getId(), result.getId());
        }

        @Test
        void testMarkAttendance_existingAttendance() {
                StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                                .studentId(student.getId())
                                .courseLessonId(lesson.getId())
                                .attendanceStatus("PRESENT")
                                .markedBy("TEACHER")
                                .build();

                when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
                when(courseLessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
                when(studentAttendanceRepository.findByStudentAndCourseLesson(student, lesson))
                                .thenReturn(Optional.of(attendance));
                when(studentAttendanceRepository.save(any())).thenReturn(attendance);
                when(studentAttendanceRepository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                               student, topic, StudentAttendance.AttendanceStatus.PRESENT))
                                .thenReturn(10L);
                when(courseProgressRepository.findByStudentAndTopic(student, topic))
                                .thenReturn(Optional.of(courseProgress));

                StudentAttendanceDTO result = courseProgressService.markAttendance(dto);
                assertNotNull(result);
                assertEquals(attendance.getId(), result.getId());
        }

        @Test
        void testGetStudentProgressForTopic_found() {
                when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
                when(courseTopicRepository.findById(topic.getId())).thenReturn(Optional.of(topic));
                when(courseProgressRepository.findByStudentAndTopic(student, topic))
                                .thenReturn(Optional.of(courseProgress));

                CourseProgressDTOs result = courseProgressService.getStudentProgressForTopic(student.getId(),
                                topic.getId());
                assertNotNull(result);
                assertEquals(student.getId(), result.getStudentId());
        }

        @Test
        void testGetStudentProgressForTopic_notFound() {
                when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
                when(courseTopicRepository.findById(topic.getId())).thenReturn(Optional.of(topic));
                when(courseProgressRepository.findByStudentAndTopic(student, topic)).thenReturn(Optional.empty());

                CourseProgressDTOs result = courseProgressService.getStudentProgressForTopic(student.getId(),
                                topic.getId());
                assertNull(result);
        }

        @Test
        void testGetOverallCourseProgress_normal() {
                when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
                when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
                when(courseTopicRepository.countByCourse(course)).thenReturn(2L);
                when(courseProgressRepository.findProgressPercentagesByStudentAndCourse(student, course))
                                .thenReturn(List.of(50.0, 100.0));

                Double result = courseProgressService.getOverallCourseProgress(student.getId(), course.getId());
                assertEquals(75.0, result);
        }

        @Test
        void testGetOverallCourseProgress_zeroTopics() {
                when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
                when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
                when(courseTopicRepository.countByCourse(course)).thenReturn(0L);

                Double result = courseProgressService.getOverallCourseProgress(student.getId(), course.getId());
                assertEquals(0.0, result);
        }

        @Test
        void testUpdateCourseProgressForTopic_invalidTotalExpected() {
                topic.setTotalExpectedLessons(0);
                lesson.setTopic(topic);
                lesson.setCourse(course);

                when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
                when(courseLessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));

                when(studentAttendanceRepository.findByStudentAndCourseLesson(student, lesson))
                                .thenReturn(Optional.empty());
                when(studentAttendanceRepository.save(any())).thenReturn(attendance);

                when(courseProgressRepository.findByStudentAndTopic(student, topic))
                                .thenReturn(Optional.of(courseProgress));
                when(studentAttendanceRepository.countByStudentAndCourseLesson_TopicAndAttendanceStatus(
                                student, topic, StudentAttendance.AttendanceStatus.PRESENT))
                                .thenReturn(1L);

                StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                                .studentId(student.getId())
                                .courseLessonId(lesson.getId())
                                .attendanceStatus("PRESENT")
                                .markedBy("TEACHER")
                                .build();

                StudentAttendanceDTO result = courseProgressService.markAttendance(dto);
                assertNotNull(result);
        }

}
