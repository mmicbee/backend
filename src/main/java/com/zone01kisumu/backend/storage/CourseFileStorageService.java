package com.zone01kisumu.backend.storage;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface CourseFileStorageService {

    String store(MultipartFile file, String folder) throws IOException;

    byte[] read(String storageKey) throws IOException;

    void delete(String storageKey) throws IOException;

    default String resolvePublicUrl(String storageKey) {
        return storageKey;
    }

    default String extractStorageKeyFromUrl(String url) {
        return url;
    }
}
