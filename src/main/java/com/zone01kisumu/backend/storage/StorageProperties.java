package com.zone01kisumu.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String provider = "local";
    private String localDirectory = "./uploads";
    private String baseUrl = "http://localhost:8080";

    private S3 s3 = new S3();

    @Data
    public static class S3 {
        private String bucket;
        private String region = "us-east-1";
        private String endpoint;
    }
}
