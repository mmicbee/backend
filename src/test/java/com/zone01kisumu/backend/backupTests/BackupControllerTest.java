package com.zone01kisumu.backend.backupTests;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.zone01kisumu.backend.controller.BackupController;
import com.zone01kisumu.backend.dto.backup.BackupManifest;
import com.zone01kisumu.backend.dto.backup.BackupSummaryDTO;
import com.zone01kisumu.backend.dto.backup.BackupVerificationDTO;
import com.zone01kisumu.backend.dto.backup.RestoreResultDTO;
import com.zone01kisumu.backend.service.ContentBackupService;

/**
 * Controller tests for administrative backup and disaster recovery endpoints.
 */
@ExtendWith(MockitoExtension.class)
class BackupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContentBackupService backupService;

    @InjectMocks
    private BackupController backupController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(backupController).build();
    }

    @Test
    void triggerBackup_shouldReturnCreatedStatus() throws Exception {
        BackupManifest manifest = BackupManifest.builder()
                .backupId("backup_20260915_001")
                .totalFiles(3)
                .totalBytes(4096L)
                .archiveChecksum("sha256dummyhash")
                .encrypted(true)
                .version("1.0")
                .build();

        when(backupService.createBackup()).thenReturn(manifest);

        mockMvc.perform(post("/api/admin/backups/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.backupId").value("backup_20260915_001"))
                .andExpect(jsonPath("$.totalFiles").value(3))
                .andExpect(jsonPath("$.encrypted").value(true));
    }

    @Test
    void listBackups_shouldReturnListOfSummaries() throws Exception {
        BackupSummaryDTO summary = BackupSummaryDTO.builder()
                .backupId("backup_20260915_001")
                .filename("backup_20260915_001.zip.enc")
                .timestamp(LocalDateTime.now())
                .totalFiles(3)
                .totalBytes(4096L)
                .encrypted(true)
                .status("AVAILABLE")
                .build();

        when(backupService.listBackups()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/admin/backups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].backupId").value("backup_20260915_001"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void verifyBackup_shouldReturnVerificationDetails() throws Exception {
        BackupVerificationDTO verification = BackupVerificationDTO.builder()
                .backupId("backup_20260915_001")
                .valid(true)
                .archiveIntegrityPassed(true)
                .verifiedFilesCount(3)
                .totalFilesCount(3)
                .message("Backup integrity verified successfully.")
                .build();

        when(backupService.verifyBackupIntegrity("backup_20260915_001")).thenReturn(verification);

        mockMvc.perform(get("/api/admin/backups/backup_20260915_001/verify")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.verifiedFilesCount").value(3));
    }

    @Test
    void restoreBackup_shouldReturnRestoreResult() throws Exception {
        RestoreResultDTO result = RestoreResultDTO.builder()
                .backupId("backup_20260915_001")
                .success(true)
                .restoredLessonsCount(3)
                .skippedLessonsCount(0)
                .failedLessonsCount(0)
                .message("Restore completed successfully.")
                .build();

        when(backupService.restoreBackup("backup_20260915_001", false)).thenReturn(result);

        mockMvc.perform(post("/api/admin/backups/backup_20260915_001/restore?overwrite=false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.restoredLessonsCount").value(3));
    }

    @Test
    void deleteBackup_shouldReturnNoContentWhenDeleted() throws Exception {
        when(backupService.deleteBackup("backup_20260915_001")).thenReturn(true);

        mockMvc.perform(delete("/api/admin/backups/backup_20260915_001"))
                .andExpect(status().isNoContent());

        verify(backupService).deleteBackup("backup_20260915_001");
    }

    @Test
    void deleteBackup_shouldReturnNotFoundWhenMissing() throws Exception {
        when(backupService.deleteBackup("backup_nonexistent")).thenReturn(false);

        mockMvc.perform(delete("/api/admin/backups/backup_nonexistent"))
                .andExpect(status().isNotFound());
    }
}
