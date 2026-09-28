package com.zone01kisumu.backend.backupTests;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zone01kisumu.backend.config.BackupConfigProperties;
import com.zone01kisumu.backend.dto.backup.BackupManifest;
import com.zone01kisumu.backend.service.BackupScheduler;
import com.zone01kisumu.backend.service.ContentBackupService;

/**
 * Tests for scheduled automated content backup execution.
 */
@ExtendWith(MockitoExtension.class)
class BackupSchedulerTest {

    @Mock
    private ContentBackupService backupService;

    @Mock
    private BackupConfigProperties backupConfig;

    @InjectMocks
    private BackupScheduler backupScheduler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void runScheduledBackup_shouldTriggerBackupWhenEnabled() throws IOException {
        when(backupConfig.isEnabled()).thenReturn(true);
        BackupManifest manifest = BackupManifest.builder()
                .backupId("backup_123")
                .totalFiles(5)
                .totalBytes(1024L)
                .build();
        when(backupService.createBackup()).thenReturn(manifest);

        backupScheduler.runScheduledBackup();

        verify(backupService).createBackup();
    }

    @Test
    void runScheduledBackup_shouldSkipWhenDisabled() throws IOException {
        when(backupConfig.isEnabled()).thenReturn(false);

        backupScheduler.runScheduledBackup();

        verify(backupService, never()).createBackup();
    }

    @Test
    void runScheduledBackup_shouldHandleExceptionsGracefully() throws IOException {
        when(backupConfig.isEnabled()).thenReturn(true);
        when(backupService.createBackup()).thenThrow(new RuntimeException("Storage disk full"));

        // Should not throw exception out of scheduler
        backupScheduler.runScheduledBackup();

        verify(backupService).createBackup();
    }
}
