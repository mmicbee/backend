package com.zone01kisumu.backend.dto.backup;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for listing available backup archives.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupSummaryDTO {

    private String backupId;
    private String filename;
    private LocalDateTime timestamp;
    private int totalFiles;
    private long totalBytes;
    private boolean encrypted;
    private String archiveChecksum;
    private String status;
}
