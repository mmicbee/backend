package com.zone01kisumu.backend.lessonCompletionTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CompletedLessonEntry;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CourseProgressResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonCompletionResultDTO;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonStatusResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.MarkCompleteRequest;

class LessonCompletionDTOsTest {

    @Test
    void testMarkCompleteRequest() {
        LocalDateTime now = LocalDateTime.now();
        MarkCompleteRequest req = MarkCompleteRequest.builder().timestamp(now).build();
        assertEquals(now, req.getTimestamp());

        MarkCompleteRequest req2 = new MarkCompleteRequest();
        req2.setTimestamp(now);
        assertEquals(now, req2.getTimestamp());
        assertNotNull(req.toString());
    }

    @Test
    void testLessonStatusResponse() {
        LocalDateTime now = LocalDateTime.now();
        LessonStatusResponse resp = LessonStatusResponse.builder()
                .completed(true)
                .completionDate(now)
                .build();

        assertTrue(resp.isCompleted());
        assertEquals(now, resp.getCompletionDate());
        assertNotNull(resp.toString());
    }

    @Test
    void testCompletedLessonEntry() {
        LocalDateTime now = LocalDateTime.now();
        CompletedLessonEntry entry = CompletedLessonEntry.builder()
                .lessonId(1L)
                .lessonName("Lesson 1")
                .courseId(2L)
                .topicId(3L)
                .completionDate(now)
                .status("COMPLETED")
                .build();

        assertEquals(1L, entry.getLessonId());
        assertEquals("Lesson 1", entry.getLessonName());
        assertEquals(2L, entry.getCourseId());
        assertEquals(3L, entry.getTopicId());
        assertEquals(now, entry.getCompletionDate());
        assertEquals("COMPLETED", entry.getStatus());
        assertNotNull(entry.toString());
    }

    @Test
    void testCourseProgressResponse() {
        CourseProgressResponse resp = CourseProgressResponse.builder()
                .studentId(5L)
                .courseId(10L)
                .totalLessons(20)
                .completedLessons(10)
                .percentageCompleted(50.0)
                .build();

        assertEquals(5L, resp.getStudentId());
        assertEquals(10L, resp.getCourseId());
        assertEquals(20, resp.getTotalLessons());
        assertEquals(10, resp.getCompletedLessons());
        assertEquals(50.0, resp.getPercentageCompleted());
        assertNotNull(resp.toString());
    }

    @Test
    void testLessonCompletionResultDTO() {
        LocalDateTime now = LocalDateTime.now();
        LessonCompletionResultDTO result = LessonCompletionResultDTO.builder()
                .id(1L)
                .studentId(2L)
                .lessonId(3L)
                .courseId(4L)
                .completed(true)
                .completionDate(now)
                .status("COMPLETED")
                .message("Success")
                .build();

        assertEquals(1L, result.getId());
        assertEquals(2L, result.getStudentId());
        assertEquals(3L, result.getLessonId());
        assertEquals(4L, result.getCourseId());
        assertTrue(result.isCompleted());
        assertEquals(now, result.getCompletionDate());
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("Success", result.getMessage());
        assertNotNull(result.toString());
    }
}
