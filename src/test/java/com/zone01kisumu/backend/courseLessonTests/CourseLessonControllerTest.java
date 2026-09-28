package com.zone01kisumu.backend.courseLessonTests;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.zone01kisumu.backend.controller.CourseLessonController;
import com.zone01kisumu.backend.dto.CourseLessonDTO;
import com.zone01kisumu.backend.service.CourseLessonService;

class CourseLessonControllerTest {

    private CourseLessonService service;
    private CourseLessonController controller;

    @BeforeEach
    void setup() {
        service = Mockito.mock(CourseLessonService.class);
        controller = new CourseLessonController(service);
    }

    @Test
    void testCreateLesson_Success() throws Exception {
        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setLessonName("Intro");

        MultipartFile file = mock(MultipartFile.class);

        CourseLessonDTO created = new CourseLessonDTO();
        created.setId(1L);

        when(service.createLesson(1L, 2L, dto, file)).thenReturn(created);

        ResponseEntity<CourseLessonDTO> response =
                controller.createLesson(1L, 2L, dto, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        verify(service).createLesson(1L, 2L, dto, file);
    }

    @Test
    void testCreateLesson_ValidationError() throws Exception {
        CourseLessonDTO dto = new CourseLessonDTO();
        MultipartFile file = mock(MultipartFile.class);

        when(service.createLesson(anyLong(), anyLong(), any(), any()))
                .thenThrow(new IllegalArgumentException("Bad input"));

        ResponseEntity<CourseLessonDTO> response =
                controller.createLesson(1L, 2L, dto, file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateLesson_FileError() throws Exception {
        CourseLessonDTO dto = new CourseLessonDTO();
        MultipartFile file = mock(MultipartFile.class);

        when(service.createLesson(anyLong(), anyLong(), any(), any()))
                .thenThrow(new IOException("File error"));

        ResponseEntity<CourseLessonDTO> response =
                controller.createLesson(1L, 2L, dto, file);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // CREATE LESSON WITH JSON LINK
    @Test
    void testCreateLessonWithLink_Success() throws Exception {
        CourseLessonDTO dto = new CourseLessonDTO();
        CourseLessonDTO created = new CourseLessonDTO();
        created.setId(10L);

        when(service.createLesson(1L, 2L, dto, null))
                .thenReturn(created);

        ResponseEntity<CourseLessonDTO> response =
                controller.createLessonWithLink(1L, 2L, dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    // UPDATE LESSON
    @Test
    void testUpdateLesson_Success() throws Exception {
        CourseLessonDTO dto = new CourseLessonDTO();
        MultipartFile file = mock(MultipartFile.class);

        CourseLessonDTO updated = new CourseLessonDTO();
        updated.setId(55L);

        when(service.updateLesson(5L, dto, file)).thenReturn(updated);

        ResponseEntity<CourseLessonDTO> response =
                controller.updateLesson(1L, 2L, 5L, dto, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(55L, response.getBody().getId());
    }

    // GET LESSON BY TOPIC
    @Test
    void testGetLessonsByTopic() {
        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setId(3L);

        when(service.getLessonsByTopicId(2L)).thenReturn(List.of(dto));

        ResponseEntity<List<CourseLessonDTO>> response =
                controller.getLessonsByTopic(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // GET LESSON BY ID
    @Test
    void testGetLessonById_Success() {
        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setId(7L);

        when(service.getLessonById(7L)).thenReturn(dto);

        ResponseEntity<CourseLessonDTO> response =
                controller.getLessonById(1L, 2L, 7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7L, response.getBody().getId());
    }

    @Test
    void testGetLessonById_NotFound() {
        when(service.getLessonById(7L))
                .thenThrow(new IllegalArgumentException());

        ResponseEntity<CourseLessonDTO> response =
                controller.getLessonById(1L, 2L, 7L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // GET LESSON CONTENT
    @Test
    void testGetLessonContent_Success() {
        byte[] content = "abc".getBytes();

        CourseLessonDTO lesson = new CourseLessonDTO();
        lesson.setLessonName("intro");
        lesson.setRecordedMediaType("VIDEO");

        when(service.getLessonContent(9L)).thenReturn(content);
        when(service.getLessonById(9L)).thenReturn(lesson);

        ResponseEntity<byte[]> response =
                controller.getLessonContent(1L, 2L, 9L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().length);
    }

    @Test
    void testGetLessonContent_NotFound() {
        when(service.getLessonContent(9L))
                .thenThrow(new IllegalArgumentException());

        ResponseEntity<byte[]> response =
                controller.getLessonContent(1L, 2L, 9L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // DELETE LESSON
    @Test
    void testDeleteLesson_Success() {
        ResponseEntity<Void> response =
                controller.deleteLesson(1L, 2L, 5L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).deleteLesson(5L);
    }

    @Test
    void testDeleteLesson_NotFound() {
        doThrow(new IllegalArgumentException()).when(service).deleteLesson(5L);

        ResponseEntity<Void> response =
                controller.deleteLesson(1L, 2L, 5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
