package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.storage.CourseFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageControllerTest {

    @Mock
    private CourseFileStorageService storageService;

    @InjectMocks
    private StorageController storageController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void testGetStoredFile_Success_Pdf() throws IOException {
        String path = "course-lessons/1/2/document.pdf";
        request.setRequestURI("/api/storage/" + path);
        byte[] data = "Sample PDF content".getBytes();
        when(storageService.read(path)).thenReturn(data);

        ResponseEntity<byte[]> response = storageController.getStoredFile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(data, response.getBody());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("document.pdf"));
    }

    @Test
    void testGetStoredFile_Success_Video() throws IOException {
        String path = "course-lessons/5/10/lesson_video.mp4";
        request.setRequestURI("/api/storage/" + path);
        byte[] data = new byte[]{1, 2, 3, 4};
        when(storageService.read(path)).thenReturn(data);

        ResponseEntity<byte[]> response = storageController.getStoredFile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(data, response.getBody());
        assertEquals("video/mp4", response.getHeaders().getContentType().toString());
    }

    @Test
    void testGetStoredFile_NotFound() throws IOException {
        String path = "course-lessons/1/2/missing.pdf";
        request.setRequestURI("/api/storage/" + path);
        when(storageService.read(path)).thenThrow(new IOException("File not found"));

        ResponseEntity<byte[]> response = storageController.getStoredFile(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetStoredFile_BlankPath() {
        request.setRequestURI("/api/storage/");

        ResponseEntity<byte[]> response = storageController.getStoredFile(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
