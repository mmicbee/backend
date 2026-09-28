package com.zone01kisumu.backend.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Descriptor of an individual backed-up media item or file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupItemDescriptor {

    private Long lessonId;
    private Long courseId;
    private Long topicId;
    private String lessonName;
    private String mediaType;
    private Long fileSize;
    private String entryPath;
    private String sha256Checksum;
}
