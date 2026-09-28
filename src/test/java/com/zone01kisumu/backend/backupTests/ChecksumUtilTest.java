package com.zone01kisumu.backend.backupTests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zone01kisumu.backend.util.ChecksumUtil;

/**
 * Tests for cryptographic SHA-256 checksum calculation and verification.
 */
class ChecksumUtilTest {

    @Test
    void testCalculateSha256ForByteArray() {
        byte[] data = "Hello LMS Backup Integrity".getBytes(StandardCharsets.UTF_8);
        String hash = ChecksumUtil.calculateSha256(data);

        assertEquals(64, hash.length());
        assertTrue(ChecksumUtil.verifySha256(data, hash));
        assertFalse(ChecksumUtil.verifySha256(data, "invalidchecksum"));
    }

    @Test
    void testCalculateSha256ForStream() throws IOException {
        byte[] data = "Streaming content for backup".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        String streamHash = ChecksumUtil.calculateSha256(bais);
        String byteHash = ChecksumUtil.calculateSha256(data);

        assertEquals(byteHash, streamHash);
    }

    @Test
    void testCalculateSha256ForFile(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("sample.pdf");
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Files.write(tempFile, fileBytes);

        String fileHash = ChecksumUtil.calculateSha256(tempFile);
        String byteHash = ChecksumUtil.calculateSha256(fileBytes);

        assertEquals(byteHash, fileHash);
    }

    @Test
    void testMismatchDetection() {
        byte[] data1 = "Original lesson video bytes".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "Tampered lesson video bytes".getBytes(StandardCharsets.UTF_8);

        String hash1 = ChecksumUtil.calculateSha256(data1);
        String hash2 = ChecksumUtil.calculateSha256(data2);

        assertNotEquals(hash1, hash2);
        assertFalse(ChecksumUtil.verifySha256(data2, hash1));
    }

    @Test
    void testNullAndEmptyInputs() throws IOException {
        assertEquals("", ChecksumUtil.calculateSha256((byte[]) null));
        assertEquals("", ChecksumUtil.calculateSha256((java.io.InputStream) null));
        assertEquals("", ChecksumUtil.calculateSha256((Path) null));
        assertFalse(ChecksumUtil.verifySha256(new byte[0], null));
    }
}
