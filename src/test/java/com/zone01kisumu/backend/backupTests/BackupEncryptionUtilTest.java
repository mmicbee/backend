package com.zone01kisumu.backend.backupTests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zone01kisumu.backend.util.BackupEncryptionUtil;

/**
 * Tests for AES-256-GCM encryption and decryption of backup archives.
 */
class BackupEncryptionUtilTest {

    private static final String SECRET = "StrongLmsBackupSecretPassphrase2026!";

    @Test
    void testEncryptAndDecryptByteArray() throws GeneralSecurityException {
        String originalText = "Top secret course lesson video and slide archive content";
        byte[] plainBytes = originalText.getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = BackupEncryptionUtil.encrypt(plainBytes, SECRET);

        assertNotNull(encrypted);
        assertFalse(new String(encrypted, StandardCharsets.UTF_8).contains("Top secret"));

        byte[] decrypted = BackupEncryptionUtil.decrypt(encrypted, SECRET);
        assertArrayEquals(plainBytes, decrypted);
        assertEquals(originalText, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void testDecryptionFailsWithWrongPassphrase() throws GeneralSecurityException {
        byte[] plainBytes = "Confidential LMS Data".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = BackupEncryptionUtil.encrypt(plainBytes, SECRET);

        assertThrows(GeneralSecurityException.class, () -> {
            BackupEncryptionUtil.decrypt(encrypted, "WrongPassword123!");
        });
    }

    @Test
    void testTamperedCiphertextFailsAuthentication() throws GeneralSecurityException {
        byte[] plainBytes = "Authenticated Data Verification".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = BackupEncryptionUtil.encrypt(plainBytes, SECRET);

        // Tamper with one byte in the ciphertext payload
        encrypted[encrypted.length - 1] ^= 0xFF;

        assertThrows(GeneralSecurityException.class, () -> {
            BackupEncryptionUtil.decrypt(encrypted, SECRET);
        });
    }

    @Test
    void testEncryptAndDecryptFile(@TempDir Path tempDir) throws Exception {
        Path plainFile = tempDir.resolve("original.zip");
        Path encryptedFile = tempDir.resolve("backup.zip.enc");
        Path decryptedFile = tempDir.resolve("restored.zip");

        byte[] sampleZipBytes = new byte[]{0x50, 0x4B, 0x03, 0x04, 10, 20, 30, 40, 50};
        Files.write(plainFile, sampleZipBytes);

        BackupEncryptionUtil.encryptFile(plainFile, encryptedFile, SECRET);
        assertTrue(Files.exists(encryptedFile));
        assertFalse(Files.size(encryptedFile) == 0);

        BackupEncryptionUtil.decryptFile(encryptedFile, decryptedFile, SECRET);
        assertTrue(Files.exists(decryptedFile));

        byte[] restoredBytes = Files.readAllBytes(decryptedFile);
        assertArrayEquals(sampleZipBytes, restoredBytes);
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}
