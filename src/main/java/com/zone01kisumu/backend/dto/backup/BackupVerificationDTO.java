package com.zone01kisumu.backend.dto.backup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a backup integrity verification check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupVerificationDTO {

    private String backupId;
    private boolean valid;
    private boolean archiveIntegrityPassed;
    private int verifiedFilesCount;
    private int totalFilesCount;
    private LocalDateTime verifiedAt;
    private String message;

    @Builder.Default
    private List<String> errorDetails = new ArrayList<>();
}
