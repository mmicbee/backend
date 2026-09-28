package com.zone01kisumu.backend.dto.backup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata manifest describing the entire backup archive and its contents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupManifest {

    private String backupId;
    private LocalDateTime timestamp;
    private int totalFiles;
    private long totalBytes;
    private String archiveChecksum;
    private boolean encrypted;
    private String version;

    @Builder.Default
    private List<BackupItemDescriptor> items = new ArrayList<>();
}
