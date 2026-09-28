package com.zone01kisumu.backend.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalCourseFileStorageService implements CourseFileStorageService {

    private final StorageProperties properties;
    private final Path rootDirectory;

    public LocalCourseFileStorageService(StorageProperties properties) throws IOException {
        this.properties = properties;
        this.rootDirectory = Path.of(properties.getLocalDirectory()).toAbsolutePath().normalize();
        Files.createDirectories(rootDirectory);
    }

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Uploaded file is empty.");
        }

        String sanitizedFolder = sanitizePath(folder);
        Path targetDirectory = rootDirectory.resolve(sanitizedFolder).normalize();
        Files.createDirectories(targetDirectory);

        String originalName = Objects.requireNonNull(file.getOriginalFilename(), "Filename is required");
        String cleanName = sanitizeFileName(originalName);
        String storageKey = sanitizedFolder + "/" + UUID.randomUUID() + "_" + cleanName;
        Path targetFile = rootDirectory.resolve(storageKey).normalize();

        try (var input = file.getInputStream()) {
            Files.copy(input, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return storageKey;
    }

    @Override
    public byte[] read(String storageKey) throws IOException {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IOException("Storage key is required.");
        }
        Path filePath = rootDirectory.resolve(sanitizePath(storageKey)).normalize();
        if (!filePath.startsWith(rootDirectory)) {
            throw new IOException("Invalid storage key path.");
        }
        return Files.readAllBytes(filePath);
    }

    @Override
    public void delete(String storageKey) throws IOException {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        Path filePath = rootDirectory.resolve(sanitizePath(storageKey)).normalize();
        if (filePath.startsWith(rootDirectory)) {
            Files.deleteIfExists(filePath);
        }
    }

    @Override
    public String resolvePublicUrl(String storageKey) {
        String normalizedKey = sanitizePath(storageKey);
        return properties.getBaseUrl().replaceAll("/+$", "") + "/api/storage/" + normalizedKey;
    }

    @Override
    public String extractStorageKeyFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String normalized = url.trim();
        if (normalized.startsWith("storage://")) {
            return normalized.substring("storage://".length());
        }
        String baseUrl = properties.getBaseUrl().replaceAll("/+$", "");
        if (normalized.startsWith(baseUrl + "/api/storage/")) {
            return normalized.substring((baseUrl + "/api/storage/").length());
        }
        if (normalized.startsWith("/api/storage/")) {
            return normalized.substring("/api/storage/".length());
        }
        return normalized;
    }

    private String sanitizePath(String value) {
        String cleaned = StringUtils.cleanPath(Objects.requireNonNull(value, "Path is required"));
        return cleaned.replace("..", "").replace("\\", "/");
    }

    private String sanitizeFileName(String fileName) {
        String clean = StringUtils.cleanPath(fileName);
        return clean.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
