package com.zone01kisumu.backend.lessonCompletionTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.zone01kisumu.backend.controller.LessonCompletionController;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CompletedLessonEntry;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.CourseProgressResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonCompletionResultDTO;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.LessonStatusResponse;
import com.zone01kisumu.backend.dto.LessonCompletionDTOs.MarkCompleteRequest;
import com.zone01kisumu.backend.service.LessonCompletionService;

@ExtendWith(MockitoExtension.class)
class LessonCompletionControllerTest {

    @Mock
    private LessonCompletionService lessonCompletionService;

    @InjectMocks
    private LessonCompletionController lessonCompletionController;

    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2026, 9, 15, 10, 0);
    }

    @Test
    void markComplete_returnsOk_onSuccess() {
        MarkCompleteRequest request = MarkCompleteRequest.builder().timestamp(testTime).build();
        LessonCompletionResultDTO dto = LessonCompletionResultDTO.builder()
                .id(1L)
                .studentId(2L)
                .lessonId(3L)
                .courseId(4L)
                .completed(true)
                .completionDate(testTime)
                .status("COMPLETED")
                .message("Success")
                .build();

        when(lessonCompletionService.markComplete(2L, 3L, testTime)).thenReturn(dto);

        ResponseEntity<LessonCompletionResultDTO> response =
                lessonCompletionController.markComplete(2L, 3L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(lessonCompletionService).markComplete(2L, 3L, testTime);
    }

    @Test
    void markComplete_returnsConflict_onIllegalStateException() {
        when(lessonCompletionService.markComplete(eq(2L), eq(3L), any()))
                .thenThrow(new IllegalStateException("Already completed"));

        ResponseEntity<LessonCompletionResultDTO> response =
                lessonCompletionController.markComplete(2L, 3L, null);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void markComplete_returnsNotFound_onIllegalArgumentException() {
        when(lessonCompletionService.markComplete(eq(2L), eq(3L), any()))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<LessonCompletionResultDTO> response =
                lessonCompletionController.markComplete(2L, 3L, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void markComplete_returnsInternalError_onGenericException() {
        when(lessonCompletionService.markComplete(eq(2L), eq(3L), any()))
                .thenThrow(new RuntimeException("DB Error"));

        ResponseEntity<LessonCompletionResultDTO> response =
                lessonCompletionController.markComplete(2L, 3L, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void unmarkComplete_returnsNoContent_onSuccess() {
        ResponseEntity<Void> response = lessonCompletionController.unmarkComplete(2L, 3L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(lessonCompletionService).unmarkComplete(2L, 3L);
    }

    @Test
    void unmarkComplete_returnsNotFound_onIllegalArgumentException() {
        doThrow(new IllegalArgumentException("Not found"))
                .when(lessonCompletionService).unmarkComplete(2L, 3L);

        ResponseEntity<Void> response = lessonCompletionController.unmarkComplete(2L, 3L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void unmarkComplete_returnsInternalError_onGenericException() {
        doThrow(new RuntimeException("DB Error"))
                .when(lessonCompletionService).unmarkComplete(2L, 3L);

        ResponseEntity<Void> response = lessonCompletionController.unmarkComplete(2L, 3L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getStatus_returnsOk_onSuccess() {
        LessonStatusResponse resp = LessonStatusResponse.builder()
                .completed(true)
                .completionDate(testTime)
                .build();
        when(lessonCompletionService.getStatus(2L, 3L)).thenReturn(resp);

        ResponseEntity<LessonStatusResponse> response = lessonCompletionController.getStatus(2L, 3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isCompleted());
    }

    @Test
    void getStatus_returnsNotFound_onIllegalArgumentException() {
        when(lessonCompletionService.getStatus(2L, 3L))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<LessonStatusResponse> response = lessonCompletionController.getStatus(2L, 3L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getStatus_returnsInternalError_onGenericException() {
        when(lessonCompletionService.getStatus(2L, 3L))
                .thenThrow(new RuntimeException("DB Error"));

        ResponseEntity<LessonStatusResponse> response = lessonCompletionController.getStatus(2L, 3L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCompletedLessons_returnsOk_onSuccess() {
        CompletedLessonEntry entry = CompletedLessonEntry.builder()
                .lessonId(3L)
                .lessonName("Intro")
                .completionDate(testTime)
                .build();
        when(lessonCompletionService.getCompletedLessons(2L, 10L)).thenReturn(List.of(entry));

        ResponseEntity<List<CompletedLessonEntry>> response =
                lessonCompletionController.getCompletedLessons(2L, 10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getCompletedLessons_returnsNotFound_onIllegalArgumentException() {
        when(lessonCompletionService.getCompletedLessons(2L, 10L))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<List<CompletedLessonEntry>> response =
                lessonCompletionController.getCompletedLessons(2L, 10L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCompletedLessons_returnsInternalError_onGenericException() {
        when(lessonCompletionService.getCompletedLessons(2L, 10L))
                .thenThrow(new RuntimeException("DB Error"));

        ResponseEntity<List<CompletedLessonEntry>> response =
                lessonCompletionController.getCompletedLessons(2L, 10L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCourseProgress_returnsOk_onSuccess() {
        CourseProgressResponse resp = CourseProgressResponse.builder()
                .studentId(2L)
                .courseId(10L)
                .totalLessons(5)
                .completedLessons(3)
                .percentageCompleted(60.0)
                .build();
        when(lessonCompletionService.getCourseProgress(2L, 10L)).thenReturn(resp);

        ResponseEntity<CourseProgressResponse> response =
                lessonCompletionController.getCourseProgress(2L, 10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(60.0, response.getBody().getPercentageCompleted());
    }

    @Test
    void getCourseProgress_returnsNotFound_onIllegalArgumentException() {
        when(lessonCompletionService.getCourseProgress(2L, 10L))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<CourseProgressResponse> response =
                lessonCompletionController.getCourseProgress(2L, 10L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCourseProgress_returnsInternalError_onGenericException() {
        when(lessonCompletionService.getCourseProgress(2L, 10L))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<CourseProgressResponse> response =
                lessonCompletionController.getCourseProgress(2L, 10L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
