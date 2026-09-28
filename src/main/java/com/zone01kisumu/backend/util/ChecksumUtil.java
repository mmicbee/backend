package com.zone01kisumu.backend.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for calculating and validating cryptographic SHA-256 checksums.
 */
public final class ChecksumUtil {

    private static final String SHA_256 = "SHA-256";
    private static final int BUFFER_SIZE = 8192;

    private ChecksumUtil() {
        // Utility class
    }

    /**
     * Calculates SHA-256 checksum for raw byte array.
     *
     * @param data The byte array to digest.
     * @return Hex-encoded SHA-256 checksum.
     */
    public static String calculateSha256(byte[] data) {
        if (data == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(data);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Calculates SHA-256 checksum for an input stream.
     *
     * @param inputStream The stream to read from.
     * @return Hex-encoded SHA-256 checksum.
     * @throws IOException If stream reading fails.
     */
    public static String calculateSha256(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Calculates SHA-256 checksum for a file on disk.
     *
     * @param filePath Path to the file.
     * @return Hex-encoded SHA-256 checksum.
     * @throws IOException If file reading fails.
     */
    public static String calculateSha256(Path filePath) throws IOException {
        if (filePath == null || !Files.exists(filePath)) {
            return "";
        }
        try (InputStream is = Files.newInputStream(filePath)) {
            return calculateSha256(is);
        }
    }

    /**
     * Validates if data matches expected checksum.
     *
     * @param data Raw byte array.
     * @param expectedChecksum Expected hex string.
     * @return True if hashes match case-insensitively.
     */
    public static boolean verifySha256(byte[] data, String expectedChecksum) {
        if (expectedChecksum == null) {
            return false;
        }
        String computed = calculateSha256(data);
        return computed.equalsIgnoreCase(expectedChecksum.trim());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
