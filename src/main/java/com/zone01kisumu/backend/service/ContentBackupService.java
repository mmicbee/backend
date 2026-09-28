package com.zone01kisumu.backend.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zone01kisumu.backend.config.BackupConfigProperties;
import com.zone01kisumu.backend.dto.backup.BackupItemDescriptor;
import com.zone01kisumu.backend.dto.backup.BackupManifest;
import com.zone01kisumu.backend.dto.backup.BackupSummaryDTO;
import com.zone01kisumu.backend.dto.backup.BackupVerificationDTO;
import com.zone01kisumu.backend.dto.backup.RestoreResultDTO;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.repository.CourseLessonRepository;
import com.zone01kisumu.backend.util.BackupEncryptionUtil;
import com.zone01kisumu.backend.util.ChecksumUtil;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service managing automated backup, integrity verification, and disaster recovery
 * for uploaded course media and lesson content.
 */
@Service
@RequiredArgsConstructor
public class ContentBackupService {

    private final BackupConfigProperties backupConfig;
    private final CourseLessonRepository courseLessonRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Creates a new backup archive of all uploaded lesson media and metadata.
     *
     * @return Completed BackupManifest.
     * @throws IOException If file packaging fails.
     */
    @Transactional(readOnly = true)
    public BackupManifest createBackup() throws IOException {
        if (!backupConfig.isEnabled()) {
            throw new IllegalStateException("Backup functionality is disabled in configuration.");
        }

        Path storageDir = getStoragePath();
        Files.createDirectories(storageDir);

        String timestampStr = LocalDateTime.now().format(DATE_FORMATTER);
        String backupId = "backup_" + timestampStr + "_" + UUID.randomUUID().toString().substring(0, 8);

        List<CourseLesson> lessons = courseLessonRepository.findAll();
        List<BackupItemDescriptor> itemDescriptors = new ArrayList<>();
        long totalBytes = 0;

        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipBaos, StandardCharsets.UTF_8)) {
            for (CourseLesson lesson : lessons) {
                if (lesson.getRecordedMedia() != null && lesson.getRecordedMedia().length > 0) {
                    byte[] mediaBytes = lesson.getRecordedMedia();
                    String checksum = ChecksumUtil.calculateSha256(mediaBytes);
                    String sanitizedName = sanitizeFilename(lesson.getLessonName());
                    String entryPath = "media/" + lesson.getId() + "_" + sanitizedName + ".bin";

                    ZipEntry zipEntry = new ZipEntry(entryPath);
                    zos.putNextEntry(zipEntry);
                    zos.write(mediaBytes);
                    zos.closeEntry();

                    totalBytes += mediaBytes.length;

                    String mediaTypeName = lesson.getRecordedMediaType() != null
                            ? lesson.getRecordedMediaType().name() : null;

                    BackupItemDescriptor descriptor = BackupItemDescriptor.builder()
                            .lessonId(lesson.getId())
                            .courseId(lesson.getCourseId())
                            .topicId(lesson.getTopicId())
                            .lessonName(lesson.getLessonName())
                            .mediaType(mediaTypeName)
                            .fileSize(lesson.getFileSize() != null ? lesson.getFileSize() : (long) mediaBytes.length)
                            .entryPath(entryPath)
                            .sha256Checksum(checksum)
                            .build();

                    itemDescriptors.add(descriptor);
                }
            }

            // Write Manifest JSON inside ZIP
            BackupManifest manifest = BackupManifest.builder()
                    .backupId(backupId)
                    .timestamp(LocalDateTime.now())
                    .totalFiles(itemDescriptors.size())
                    .totalBytes(totalBytes)
                    .encrypted(backupConfig.isEncryptionEnabled())
                    .version("1.0")
                    .items(itemDescriptors)
                    .build();

            byte[] manifestJsonBytes = getObjectMapper().writeValueAsBytes(manifest);
            ZipEntry manifestEntry = new ZipEntry("manifest.json");
            zos.putNextEntry(manifestEntry);
            zos.write(manifestJsonBytes);
            zos.closeEntry();
        }

        byte[] unencryptedZipBytes = zipBaos.toByteArray();
        String archiveChecksum = ChecksumUtil.calculateSha256(unencryptedZipBytes);

        BackupManifest finalManifest = BackupManifest.builder()
                .backupId(backupId)
                .timestamp(LocalDateTime.now())
                .totalFiles(itemDescriptors.size())
                .totalBytes(totalBytes)
                .archiveChecksum(archiveChecksum)
                .encrypted(backupConfig.isEncryptionEnabled())
                .version("1.0")
                .items(itemDescriptors)
                .build();

        // Save archive (encrypted or plaintext)
        byte[] finalArchiveBytes = unencryptedZipBytes;
        String archiveFilename = backupId + (backupConfig.isEncryptionEnabled() ? ".zip.enc" : ".zip");
        if (backupConfig.isEncryptionEnabled()) {
            try {
                finalArchiveBytes = BackupEncryptionUtil.encrypt(
                        unencryptedZipBytes, backupConfig.getEncryptionSecret());
            } catch (Exception e) {
                throw new IOException("Failed to encrypt backup archive", e);
            }
        }

        Path archivePath = storageDir.resolve(archiveFilename);
        Files.write(archivePath, finalArchiveBytes);

        // Save standalone manifest JSON next to archive
        Path manifestPath = storageDir.resolve(backupId + "_manifest.json");
        Files.write(manifestPath, getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(finalManifest));

        LoggerUtil.logInfo("Successfully created backup [{}] with {} files, {} bytes",
                backupId, itemDescriptors.size(), totalBytes);

        // Prune old backups per retention policy
        pruneOldBackups();

        return finalManifest;
    }

    /**
     * Lists all available backup summaries from the storage directory.
     *
     * @return List of BackupSummaryDTO sorted by timestamp descending.
     */
    public List<BackupSummaryDTO> listBackups() {
        Path storageDir = getStoragePath();
        List<BackupSummaryDTO> summaries = new ArrayList<>();

        if (!Files.exists(storageDir)) {
            return summaries;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*_manifest.json")) {
            for (Path manifestFile : stream) {
                try {
                    BackupManifest manifest = getObjectMapper().readValue(manifestFile.toFile(), BackupManifest.class);
                    String archiveFilename = manifest.getBackupId()
                            + (manifest.isEncrypted() ? ".zip.enc" : ".zip");
                    Path archiveFile = storageDir.resolve(archiveFilename);

                    summaries.add(BackupSummaryDTO.builder()
                            .backupId(manifest.getBackupId())
                            .filename(archiveFilename)
                            .timestamp(manifest.getTimestamp())
                            .totalFiles(manifest.getTotalFiles())
                            .totalBytes(manifest.getTotalBytes())
                            .encrypted(manifest.isEncrypted())
                            .archiveChecksum(manifest.getArchiveChecksum())
                            .status(Files.exists(archiveFile) ? "AVAILABLE" : "ARCHIVE_MISSING")
                            .build());
                } catch (Exception e) {
                    LoggerUtil.logError("Failed to read manifest file: {}", manifestFile, e);
                }
            }
        } catch (IOException e) {
            LoggerUtil.logError("Error listing backups in directory: {}", storageDir, e);
        }

        summaries.sort(Comparator.comparing(BackupSummaryDTO::getTimestamp).reversed());
        return summaries;
    }

    /**
     * Verifies the cryptographic integrity of a backup archive against its manifest.
     *
     * @param backupId Unique ID of the backup.
     * @return BackupVerificationDTO.
     */
    public BackupVerificationDTO verifyBackupIntegrity(String backupId) {
        Path storageDir = getStoragePath();
        Path manifestPath = storageDir.resolve(backupId + "_manifest.json");

        if (!Files.exists(manifestPath)) {
            return BackupVerificationDTO.builder()
                    .backupId(backupId)
                    .valid(false)
                    .verifiedAt(LocalDateTime.now())
                    .message("Manifest file not found for backup: " + backupId)
                    .build();
        }

        try {
            BackupManifest manifest = getObjectMapper().readValue(manifestPath.toFile(), BackupManifest.class);
            String archiveFilename = backupId + (manifest.isEncrypted() ? ".zip.enc" : ".zip");
            Path archivePath = storageDir.resolve(archiveFilename);

            if (!Files.exists(archivePath)) {
                return BackupVerificationDTO.builder()
                        .backupId(backupId)
                        .valid(false)
                        .verifiedAt(LocalDateTime.now())
                        .message("Backup archive file not found: " + archiveFilename)
                        .build();
            }

            byte[] archiveBytes = Files.readAllBytes(archivePath);
            byte[] rawZipBytes = archiveBytes;

            if (manifest.isEncrypted()) {
                rawZipBytes = BackupEncryptionUtil.decrypt(archiveBytes, backupConfig.getEncryptionSecret());
            }

            // Verify whole archive checksum
            String computedArchiveChecksum = ChecksumUtil.calculateSha256(rawZipBytes);
            boolean archiveChecksumMatches = computedArchiveChecksum.equalsIgnoreCase(manifest.getArchiveChecksum());

            List<String> errors = new ArrayList<>();
            if (!archiveChecksumMatches) {
                errors.add("Archive checksum mismatch: expected=" + manifest.getArchiveChecksum()
                        + ", actual=" + computedArchiveChecksum);
            }

            // Verify internal items
            int verifiedFiles = 0;
            try (ZipInputStream zis = new ZipInputStream(
                    new ByteArrayInputStream(rawZipBytes), StandardCharsets.UTF_8)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().startsWith("media/")) {
                        final String entryName = entry.getName();
                        byte[] entryBytes = zis.readAllBytes();
                        String entryChecksum = ChecksumUtil.calculateSha256(entryBytes);

                        Optional<BackupItemDescriptor> itemOpt = manifest.getItems().stream()
                                .filter(item -> entryName.equals(item.getEntryPath()))
                                .findFirst();

                        if (itemOpt.isPresent()) {
                            if (entryChecksum.equalsIgnoreCase(itemOpt.get().getSha256Checksum())) {
                                verifiedFiles++;
                            } else {
                                errors.add("File checksum mismatch for " + entry.getName()
                                        + ": expected=" + itemOpt.get().getSha256Checksum()
                                        + ", actual=" + entryChecksum);
                            }
                        } else {
                            errors.add("Unexpected file found in archive: " + entry.getName());
                        }
                    }
                    zis.closeEntry();
                }
            }

            boolean isValid = errors.isEmpty() && verifiedFiles == manifest.getTotalFiles();
            String verificationMsg = isValid
                    ? "Backup integrity verified successfully."
                    : "Integrity check failed with errors.";

            return BackupVerificationDTO.builder()
                    .backupId(backupId)
                    .valid(isValid)
                    .archiveIntegrityPassed(archiveChecksumMatches)
                    .verifiedFilesCount(verifiedFiles)
                    .totalFilesCount(manifest.getTotalFiles())
                    .verifiedAt(LocalDateTime.now())
                    .message(verificationMsg)
                    .errorDetails(errors)
                    .build();

        } catch (Exception e) {
            LoggerUtil.logError("Integrity verification error for backup [{}]", backupId, e);
            return BackupVerificationDTO.builder()
                    .backupId(backupId)
                    .valid(false)
                    .verifiedAt(LocalDateTime.now())
                    .message("Integrity verification failed: " + e.getMessage())
                    .errorDetails(List.of(e.getMessage() != null ? e.getMessage() : "Unknown error"))
                    .build();
        }
    }

    /**
     * Restores lesson media and records from a backup archive.
     *
     * @param backupId Backup ID to restore.
     * @param overwriteExisting Whether to overwrite existing media.
     * @return RestoreResultDTO.
     */
    @Transactional
    public RestoreResultDTO restoreBackup(String backupId, boolean overwriteExisting) {
        long startTime = System.currentTimeMillis();
        Path storageDir = getStoragePath();
        Path manifestPath = storageDir.resolve(backupId + "_manifest.json");

        if (!Files.exists(manifestPath)) {
            return RestoreResultDTO.builder()
                    .backupId(backupId)
                    .success(false)
                    .message("Manifest not found for backup: " + backupId)
                    .completedAt(LocalDateTime.now())
                    .executionDurationMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        try {
            BackupManifest manifest = getObjectMapper().readValue(manifestPath.toFile(), BackupManifest.class);
            String archiveFilename = backupId + (manifest.isEncrypted() ? ".zip.enc" : ".zip");
            Path archivePath = storageDir.resolve(archiveFilename);

            if (!Files.exists(archivePath)) {
                return RestoreResultDTO.builder()
                        .backupId(backupId)
                        .success(false)
                        .message("Archive file not found: " + archiveFilename)
                        .completedAt(LocalDateTime.now())
                        .executionDurationMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            byte[] archiveBytes = Files.readAllBytes(archivePath);
            byte[] rawZipBytes = archiveBytes;

            if (manifest.isEncrypted()) {
                rawZipBytes = BackupEncryptionUtil.decrypt(archiveBytes, backupConfig.getEncryptionSecret());
            }

            int restoredCount = 0;
            int skippedCount = 0;
            int failedCount = 0;
            List<String> details = new ArrayList<>();

            try (ZipInputStream zis = new ZipInputStream(
                    new ByteArrayInputStream(rawZipBytes), StandardCharsets.UTF_8)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().startsWith("media/")) {
                        byte[] mediaData = zis.readAllBytes();
                        String entryName = entry.getName();

                        Optional<BackupItemDescriptor> itemOpt = manifest.getItems().stream()
                                .filter(item -> entryName.equals(item.getEntryPath()))
                                .findFirst();

                        if (itemOpt.isPresent()) {
                            BackupItemDescriptor item = itemOpt.get();
                            Optional<CourseLesson> lessonOpt = courseLessonRepository.findById(item.getLessonId());

                            if (lessonOpt.isPresent()) {
                                CourseLesson lesson = lessonOpt.get();
                                boolean hasNoMedia = lesson.getRecordedMedia() == null
                                        || lesson.getRecordedMedia().length == 0;
                                if (hasNoMedia || overwriteExisting) {
                                    lesson.setRecordedMedia(mediaData);
                                    if (item.getMediaType() != null) {
                                        try {
                                            lesson.setRecordedMediaType(
                                                    CourseLesson.RecordedMediaType.valueOf(item.getMediaType()));
                                        } catch (IllegalArgumentException ignored) {
                                            lesson.setRecordedMediaType(CourseLesson.RecordedMediaType.OTHER);
                                        }
                                    }
                                    courseLessonRepository.save(lesson);
                                    restoredCount++;
                                    details.add("Restored media for lesson ID " + item.getLessonId()
                                            + " (" + item.getLessonName() + ")");
                                } else {
                                    skippedCount++;
                                    details.add("Skipped existing lesson ID " + item.getLessonId()
                                            + " (overwrite=false)");
                                }
                            } else {
                                skippedCount++;
                                details.add("Lesson ID " + item.getLessonId() + " does not exist in DB, skipped.");
                            }
                        } else {
                            failedCount++;
                            details.add("Unmatched entry in archive: " + entryName);
                        }
                    }
                    zis.closeEntry();
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            LoggerUtil.logInfo("Restore completed for backup [{}] in {} ms. Restored: {}, Skipped: {}, Failed: {}",
                    backupId, duration, restoredCount, skippedCount, failedCount);

            return RestoreResultDTO.builder()
                    .backupId(backupId)
                    .success(true)
                    .restoredLessonsCount(restoredCount)
                    .skippedLessonsCount(skippedCount)
                    .failedLessonsCount(failedCount)
                    .executionDurationMs(duration)
                    .completedAt(LocalDateTime.now())
                    .message("Restore completed successfully.")
                    .details(details)
                    .build();

        } catch (Exception e) {
            LoggerUtil.logError("Failed to restore backup [{}]", backupId, e);
            return RestoreResultDTO.builder()
                    .backupId(backupId)
                    .success(false)
                    .executionDurationMs(System.currentTimeMillis() - startTime)
                    .completedAt(LocalDateTime.now())
                    .message("Restore operation failed: " + e.getMessage())
                    .details(List.of(e.getMessage() != null ? e.getMessage() : "Unknown error"))
                    .build();
        }
    }

    /**
     * Deletes a specific backup archive and manifest.
     *
     * @param backupId Backup ID to delete.
     * @return True if deleted.
     */
    public boolean deleteBackup(String backupId) {
        Path storageDir = getStoragePath();
        boolean deletedAny = false;

        try {
            Path manifestPath = storageDir.resolve(backupId + "_manifest.json");
            if (Files.deleteIfExists(manifestPath)) {
                deletedAny = true;
            }

            Path zipPath = storageDir.resolve(backupId + ".zip");
            if (Files.deleteIfExists(zipPath)) {
                deletedAny = true;
            }

            Path encPath = storageDir.resolve(backupId + ".zip.enc");
            if (Files.deleteIfExists(encPath)) {
                deletedAny = true;
            }
        } catch (IOException e) {
            LoggerUtil.logError("Error deleting backup [{}]", backupId, e);
        }

        return deletedAny;
    }

    /**
     * Prunes old backup archives exceeding retention limit.
     */
    public void pruneOldBackups() {
        int maxCount = backupConfig.getRetentionMaxCount();
        if (maxCount <= 0) {
            return;
        }

        List<BackupSummaryDTO> backups = listBackups();
        if (backups.size() > maxCount) {
            List<BackupSummaryDTO> toDelete = backups.subList(maxCount, backups.size());
            for (BackupSummaryDTO summary : toDelete) {
                LoggerUtil.logInfo("Pruning expired backup archive [{}] per retention policy", summary.getBackupId());
                deleteBackup(summary.getBackupId());
            }
        }
    }

    private Path getStoragePath() {
        return Paths.get(backupConfig.getStorageDirectory());
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private String sanitizeFilename(String name) {
        if (name == null) {
            return "unnamed";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
