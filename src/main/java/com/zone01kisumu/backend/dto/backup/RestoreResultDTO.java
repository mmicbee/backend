package com.zone01kisumu.backend.dto.backup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary of a disaster recovery restore execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreResultDTO {

    private String backupId;
    private boolean success;
    private int restoredLessonsCount;
    private int skippedLessonsCount;
    private int failedLessonsCount;
    private long executionDurationMs;
    private LocalDateTime completedAt;
    private String message;

    @Builder.Default
    private List<String> details = new ArrayList<>();
}
