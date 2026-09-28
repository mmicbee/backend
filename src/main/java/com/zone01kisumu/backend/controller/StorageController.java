package com.zone01kisumu.backend.controller;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.storage.CourseFileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StorageController {

    private final CourseFileStorageService courseFileStorageService;

    @GetMapping("/storage/**")
    public ResponseEntity<byte[]> getStoredFile(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String prefix = "/api/storage/";
        int idx = requestUri.indexOf(prefix);
        String path = idx != -1 ? requestUri.substring(idx + prefix.length()) : "";

        if (path.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] content = courseFileStorageService.read(path);
            String fileName = Path.of(path).getFileName().toString();
            String contentType = determineContentType(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(content.length);
            headers.setContentDisposition(
                    ContentDisposition.inline()
                            .filename(fileName)
                            .build()
            );

            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String fileName) {
        if (fileName == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".txt")) return "text/plain";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
