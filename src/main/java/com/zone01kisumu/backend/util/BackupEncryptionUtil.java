package com.zone01kisumu.backend.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility for AES-256-GCM authenticated encryption and decryption of backup archives.
 */
public final class BackupEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private BackupEncryptionUtil() {
        // Utility class
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM derived from a passphrase.
     *
     * @param plaintext Data to encrypt.
     * @param passphrase Password / secret key.
     * @return Formatted payload containing [Salt (16B) | IV (12B) | Ciphertext + Tag].
     * @throws GeneralSecurityException If encryption fails.
     */
    public static byte[] encrypt(byte[] plaintext, String passphrase) throws GeneralSecurityException {
        if (plaintext == null || passphrase == null) {
            throw new IllegalArgumentException("Plaintext and passphrase must not be null");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);

        SecretKey secretKey = deriveKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] cipherText = cipher.doFinal(plaintext);

        ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + cipherText.length);
        byteBuffer.put(salt);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return byteBuffer.array();
    }

    /**
     * Decrypts encrypted payload containing [Salt (16B) | IV (12B) | Ciphertext + Tag].
     *
     * @param encryptedData Encrypted payload.
     * @param passphrase Password / secret key used for encryption.
     * @return Decrypted plaintext bytes.
     * @throws GeneralSecurityException If decryption or authentication verification fails.
     */
    public static byte[] decrypt(byte[] encryptedData, String passphrase) throws GeneralSecurityException {
        if (encryptedData == null || passphrase == null) {
            throw new IllegalArgumentException("Encrypted data and passphrase must not be null");
        }

        if (encryptedData.length < SALT_LENGTH_BYTES + IV_LENGTH_BYTES + (TAG_LENGTH_BITS / 8)) {
            throw new IllegalArgumentException("Encrypted payload is too short or malformed");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        byteBuffer.get(salt);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        byteBuffer.get(iv);

        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        SecretKey secretKey = deriveKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        return cipher.doFinal(cipherText);
    }

    /**
     * Encrypts a source file to a destination file.
     *
     * @param sourcePath Unencrypted source file.
     * @param destPath Target encrypted output file.
     * @param passphrase Password for encryption.
     * @throws IOException If I/O fails.
     * @throws GeneralSecurityException If cryptographic operations fail.
     */
    public static void encryptFile(Path sourcePath, Path destPath, String passphrase)
            throws IOException, GeneralSecurityException {
        byte[] inputBytes = Files.readAllBytes(sourcePath);
        byte[] encryptedBytes = encrypt(inputBytes, passphrase);
        Files.write(destPath, encryptedBytes);
    }

    /**
     * Decrypts an encrypted file to a destination file.
     *
     * @param sourcePath Encrypted source file.
     * @param destPath Target decrypted output file.
     * @param passphrase Password used during encryption.
     * @throws IOException If I/O fails.
     * @throws GeneralSecurityException If cryptographic operations fail.
     */
    public static void decryptFile(Path sourcePath, Path destPath, String passphrase)
            throws IOException, GeneralSecurityException {
        byte[] encryptedBytes = Files.readAllBytes(sourcePath);
        byte[] decryptedBytes = decrypt(encryptedBytes, passphrase);
        Files.write(destPath, decryptedBytes);
    }

    private static SecretKey deriveKey(String passphrase, byte[] salt) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}
