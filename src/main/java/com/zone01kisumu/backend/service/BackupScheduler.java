package com.zone01kisumu.backend.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zone01kisumu.backend.config.BackupConfigProperties;
import com.zone01kisumu.backend.dto.backup.BackupManifest;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Background scheduler executing periodic automated backups of uploaded content.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "backup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BackupScheduler {

    private final ContentBackupService backupService;
    private final BackupConfigProperties backupConfig;

    /**
     * Executes automated periodic backup.
     */
    @Scheduled(cron = "${backup.schedule-cron:0 0 2 * * *}")
    public void runScheduledBackup() {
        if (!backupConfig.isEnabled()) {
            LoggerUtil.logInfo("Scheduled backup skipped: backup functionality is disabled.");
            return;
        }

        LoggerUtil.logInfo("Starting scheduled automated content backup...");
        long startTime = System.currentTimeMillis();

        try {
            BackupManifest manifest = backupService.createBackup();
            long duration = System.currentTimeMillis() - startTime;
            LoggerUtil.logInfo("Scheduled backup [{}] completed successfully in {} ms (Files: {}, Bytes: {})",
                    manifest.getBackupId(), duration, manifest.getTotalFiles(), manifest.getTotalBytes());
        } catch (Exception e) {
            LoggerUtil.logError("Scheduled content backup failed", e);
        }
    }
}
