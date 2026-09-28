package com.zone01kisumu.backend.backupTests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zone01kisumu.backend.config.BackupConfigProperties;
import com.zone01kisumu.backend.dto.backup.BackupManifest;
import com.zone01kisumu.backend.dto.backup.BackupSummaryDTO;
import com.zone01kisumu.backend.dto.backup.BackupVerificationDTO;
import com.zone01kisumu.backend.dto.backup.RestoreResultDTO;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.repository.CourseLessonRepository;
import com.zone01kisumu.backend.service.ContentBackupService;

/**
 * Unit and integration tests for ContentBackupService.
 */
@ExtendWith(MockitoExtension.class)
class ContentBackupServiceTest {

    @Mock
    private CourseLessonRepository courseLessonRepository;

    private BackupConfigProperties backupConfig;
    private ObjectMapper objectMapper;
    private ContentBackupService backupService;

    @TempDir
    private Path tempStorageDir;

    private List<CourseLesson> sampleLessons;

    @BeforeEach
    void setUp() {
        backupConfig = new BackupConfigProperties();
        backupConfig.setEnabled(true);
        backupConfig.setStorageDirectory(tempStorageDir.toString());
        backupConfig.setEncryptionEnabled(true);
        backupConfig.setEncryptionSecret("TestSecretKeyForLmsBackup2026!");
        backupConfig.setRetentionMaxCount(3);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        backupService = new ContentBackupService(backupConfig, courseLessonRepository, objectMapper);

        sampleLessons = new ArrayList<>();

        CourseLesson lesson1 = new CourseLesson();
        lesson1.setId(101L);
        lesson1.setCourseId(1L);
        lesson1.setTopicId(10L);
        lesson1.setLessonName("Introductory Video");
        lesson1.setRecordedMediaType(CourseLesson.RecordedMediaType.VIDEO);
        lesson1.setRecordedMedia("Simulated Video Binary Content Stream".getBytes(StandardCharsets.UTF_8));
        lesson1.setFileSize((long) lesson1.getRecordedMedia().length);
        sampleLessons.add(lesson1);

        CourseLesson lesson2 = new CourseLesson();
        lesson2.setId(102L);
        lesson2.setCourseId(1L);
        lesson2.setTopicId(10L);
        lesson2.setLessonName("Course Syllabus PDF");
        lesson2.setRecordedMediaType(CourseLesson.RecordedMediaType.PDF);
        lesson2.setRecordedMedia("%PDF-1.4 Simulated PDF Document Content".getBytes(StandardCharsets.UTF_8));
        lesson2.setFileSize((long) lesson2.getRecordedMedia().length);
        sampleLessons.add(lesson2);
    }

    @Test
    void createBackup_shouldGenerateEncryptedArchiveAndManifest() throws IOException {
        when(courseLessonRepository.findAll()).thenReturn(sampleLessons);

        BackupManifest manifest = backupService.createBackup();

        assertNotNull(manifest);
        assertEquals(2, manifest.getTotalFiles());
        assertTrue(manifest.getTotalBytes() > 0);
        assertTrue(manifest.isEncrypted());
        assertNotNull(manifest.getArchiveChecksum());

        Path archivePath = tempStorageDir.resolve(manifest.getBackupId() + ".zip.enc");
        Path manifestPath = tempStorageDir.resolve(manifest.getBackupId() + "_manifest.json");

        assertTrue(Files.exists(archivePath));
        assertTrue(Files.exists(manifestPath));
    }

    @Test
    void listBackups_shouldReturnAvailableBackups() throws IOException {
        when(courseLessonRepository.findAll()).thenReturn(sampleLessons);

        backupService.createBackup();

        List<BackupSummaryDTO> summaries = backupService.listBackups();
        assertNotNull(summaries);
        assertEquals(1, summaries.size());
        assertEquals(2, summaries.get(0).getTotalFiles());
        assertEquals("AVAILABLE", summaries.get(0).getStatus());
    }

    @Test
    void verifyBackupIntegrity_shouldPassForValidArchive() throws IOException {
        when(courseLessonRepository.findAll()).thenReturn(sampleLessons);

        BackupManifest manifest = backupService.createBackup();
        BackupVerificationDTO verification = backupService.verifyBackupIntegrity(manifest.getBackupId());

        assertNotNull(verification);
        assertTrue(verification.isValid());
        assertTrue(verification.isArchiveIntegrityPassed());
        assertEquals(2, verification.getVerifiedFilesCount());
        assertEquals(2, verification.getTotalFilesCount());
        assertTrue(verification.getErrorDetails().isEmpty());
    }

    @Test
    void verifyBackupIntegrity_shouldFailWhenArchiveCorrupted() throws IOException {
        when(courseLessonRepository.findAll()).thenReturn(sampleLessons);

        BackupManifest manifest = backupService.createBackup();
        Path archivePath = tempStorageDir.resolve(manifest.getBackupId() + ".zip.enc");

        // Tamper with archive content
        byte[] bytes = Files.readAllBytes(archivePath);
        bytes[bytes.length - 5] ^= 0xFF;
        Files.write(archivePath, bytes);

        BackupVerificationDTO verification = backupService.verifyBackupIntegrity(manifest.getBackupId());

        assertNotNull(verification);
        assertFalse(verification.isValid());
    }

    @Test
    void restoreBackup_shouldRestoreMediaIntoDatabase() throws IOException {
        when(courseLessonRepository.findAll()).thenReturn(sampleLessons);
        BackupManifest manifest = backupService.createBackup();

        // Simulate database entity with missing media
        CourseLesson emptyLesson = new CourseLesson();
        emptyLesson.setId(101L);
        emptyLesson.setLessonName("Introductory Video");
        emptyLesson.setRecordedMedia(null);

        when(courseLessonRepository.findById(101L)).thenReturn(Optional.of(emptyLesson));
        when(courseLessonRepository.findById(102L)).thenReturn(Optional.empty());

        RestoreResultDTO result = backupService.restoreBackup(manifest.getBackupId(), false);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRestoredLessonsCount());
        assertEquals(1, result.getSkippedLessonsCount());
        verify(courseLessonRepository).save(any(CourseLesson.class));
    }

    @Test
    void deleteBackup_shouldRemoveArchiveAndManifest() throws IOException {
        when(courseLessonRepository.findAll()).thenReturn(sampleLessons);
        BackupManifest manifest = backupService.createBackup();

        boolean deleted = backupService.deleteBackup(manifest.getBackupId());
        assertTrue(deleted);

        Path archivePath = tempStorageDir.resolve(manifest.getBackupId() + ".zip.enc");
        Path manifestPath = tempStorageDir.resolve(manifest.getBackupId() + "_manifest.json");

        assertFalse(Files.exists(archivePath));
        assertFalse(Files.exists(manifestPath));
    }

    @Test
    void pruneOldBackups_shouldEnforceRetentionLimit() throws IOException {
        when(courseLessonRepository.findAll()).thenReturn(sampleLessons);
        backupConfig.setRetentionMaxCount(2);

        backupService.createBackup();
        backupService.createBackup();
        backupService.createBackup();

        List<BackupSummaryDTO> summaries = backupService.listBackups();
        assertTrue(summaries.size() <= 2);
    }
}
