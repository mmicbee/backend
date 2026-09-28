package com.zone01kisumu.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.dto.backup.BackupManifest;
import com.zone01kisumu.backend.dto.backup.BackupSummaryDTO;
import com.zone01kisumu.backend.dto.backup.BackupVerificationDTO;
import com.zone01kisumu.backend.dto.backup.RestoreResultDTO;
import com.zone01kisumu.backend.service.ContentBackupService;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Controller providing administrative endpoints for content backup and disaster recovery.
 */
@RestController
@RequestMapping("/api/admin/backups")
@RequiredArgsConstructor
public class BackupController {

    private final ContentBackupService backupService;

    /**
     * Triggers an immediate on-demand backup.
     *
     * @return Created BackupManifest.
     */
    @PostMapping("/create")
    public ResponseEntity<BackupManifest> triggerBackup() {
        try {
            BackupManifest manifest = backupService.createBackup();
            return ResponseEntity.status(HttpStatus.CREATED).body(manifest);
        } catch (Exception e) {
            LoggerUtil.logError("Manual backup creation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lists all available backups.
     *
     * @return List of BackupSummaryDTO.
     */
    @GetMapping
    public ResponseEntity<List<BackupSummaryDTO>> listBackups() {
        List<BackupSummaryDTO> backups = backupService.listBackups();
        return ResponseEntity.ok(backups);
    }

    /**
     * Runs an integrity verification on a specific backup.
     *
     * @param backupId ID of the backup.
     * @return BackupVerificationDTO.
     */
    @GetMapping("/{backupId}/verify")
    public ResponseEntity<BackupVerificationDTO> verifyBackup(@PathVariable String backupId) {
        BackupVerificationDTO verification = backupService.verifyBackupIntegrity(backupId);
        return ResponseEntity.ok(verification);
    }

    /**
     * Restores content from a specified backup archive.
     *
     * @param backupId ID of the backup to restore.
     * @param overwrite Whether to overwrite existing media.
     * @return RestoreResultDTO.
     */
    @PostMapping("/{backupId}/restore")
    public ResponseEntity<RestoreResultDTO> restoreBackup(
            @PathVariable String backupId,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        RestoreResultDTO result = backupService.restoreBackup(backupId, overwrite);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * Deletes a specific backup archive.
     *
     * @param backupId ID of the backup to delete.
     * @return 204 No Content if deleted, 404 if not found.
     */
    @DeleteMapping("/{backupId}")
    public ResponseEntity<Void> deleteBackup(@PathVariable String backupId) {
        boolean deleted = backupService.deleteBackup(backupId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
