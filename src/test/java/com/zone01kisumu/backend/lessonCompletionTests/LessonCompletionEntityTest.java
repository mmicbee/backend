package com.zone01kisumu.backend.lessonCompletionTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.LessonCompletion;
import com.zone01kisumu.backend.model.Student;

class LessonCompletionEntityTest {

    @Test
    void testEntityBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        Student student = Student.builder().id(1L).email("student@test.com").build();
        Course course = Course.builder().id(2L).title("Java Course").build();
        CourseLesson lesson = new CourseLesson();
        lesson.setId(3L);
        lesson.setLessonName("Intro to Java");

        LessonCompletion completion = LessonCompletion.builder()
                .id(10L)
                .studentId(1L)
                .student(student)
                .lessonId(3L)
                .lesson(lesson)
                .courseId(2L)
                .course(course)
                .completionDate(now)
                .status(LessonCompletion.CompletionStatus.COMPLETED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(10L, completion.getId());
        assertEquals(1L, completion.getStudentId());
        assertEquals("student@test.com", completion.getStudent().getEmail());
        assertEquals(3L, completion.getLessonId());
        assertEquals("Intro to Java", completion.getLesson().getLessonName());
        assertEquals(2L, completion.getCourseId());
        assertEquals("Java Course", completion.getCourse().getTitle());
        assertEquals(now, completion.getCompletionDate());
        assertEquals(LessonCompletion.CompletionStatus.COMPLETED, completion.getStatus());
        assertEquals(now, completion.getCreatedAt());
        assertEquals(now, completion.getUpdatedAt());
        assertNotNull(completion.toString());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        LessonCompletion completion = new LessonCompletion();
        completion.setId(5L);
        completion.setStudentId(10L);
        completion.setLessonId(20L);
        completion.setCourseId(30L);
        completion.setStatus(LessonCompletion.CompletionStatus.IN_PROGRESS);

        assertEquals(5L, completion.getId());
        assertEquals(10L, completion.getStudentId());
        assertEquals(20L, completion.getLessonId());
        assertEquals(30L, completion.getCourseId());
        assertEquals(LessonCompletion.CompletionStatus.IN_PROGRESS, completion.getStatus());
    }

    @Test
    void testEnumValues() {
        assertEquals(LessonCompletion.CompletionStatus.COMPLETED,
                LessonCompletion.CompletionStatus.valueOf("COMPLETED"));
        assertEquals(LessonCompletion.CompletionStatus.IN_PROGRESS,
                LessonCompletion.CompletionStatus.valueOf("IN_PROGRESS"));
        assertTrue(LessonCompletion.CompletionStatus.values().length >= 2);
    }
}
