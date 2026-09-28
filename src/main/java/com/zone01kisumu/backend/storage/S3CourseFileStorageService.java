package com.zone01kisumu.backend.storage;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "s3")
public class S3CourseFileStorageService implements CourseFileStorageService {

    private final StorageProperties properties;
    private final S3Client s3Client;
    private final String bucketName;

    public S3CourseFileStorageService(StorageProperties properties) {
        this.properties = properties;
        this.bucketName = properties.getS3().getBucket();
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("app.storage.s3.bucket must be configured when using S3 storage.");
        }

        var builder = S3Client.builder()
                .region(Region.of(properties.getS3().getRegion()));

        if (properties.getS3().getEndpoint() != null && !properties.getS3().getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getS3().getEndpoint()));
        }

        this.s3Client = builder.build();
    }

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Uploaded file is empty.");
        }

        String filename = Objects.requireNonNull(
                file.getOriginalFilename(), "File name is required");
        String key = normalizeFolder(folder) + "/" + java.util.UUID.randomUUID()
                + "_" + filename;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return key;
    }

    @Override
    public byte[] read(String storageKey) throws IOException {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IOException("Storage key is required.");
        }

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storageKey)
                        .build())) {
            return response.readAllBytes();
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .build());
    }

    @Override
    public String resolvePublicUrl(String storageKey) {
        String base = properties.getS3().getEndpoint();
        if (base == null || base.isBlank()) {
            return "https://" + bucketName + ".s3." + properties.getS3().getRegion() + ".amazonaws.com/" + storageKey;
        }
        return base.replaceAll("/+$", "") + "/" + bucketName + "/" + storageKey;
    }

    @Override
    public String extractStorageKeyFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (url.startsWith("storage://")) {
            return url.substring("storage://".length());
        }
        String base = properties.getS3().getEndpoint();
        String baseUrl = base == null || base.isBlank()
                ? "https://" + bucketName + ".s3." + properties.getS3().getRegion() + ".amazonaws.com"
                : base.replaceAll("/+$", "") + "/" + bucketName;
        if (url.startsWith(baseUrl + "/")) {
            return url.substring((baseUrl + "/").length());
        }
        return url;
    }

    private String normalizeFolder(String folder) {
        String normalized = folder == null ? "course-lessons" : folder.trim().replace("\\", "/");
        return normalized.replaceAll("^/+|/+$", "");
    }
}
