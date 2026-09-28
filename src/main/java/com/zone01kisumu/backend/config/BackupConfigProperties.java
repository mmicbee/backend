package com.zone01kisumu.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for automated content backup and disaster recovery.
 */
@Configuration
@ConfigurationProperties(prefix = "backup")
@Data
public class BackupConfigProperties {

    /**
     * Master toggle for automated and on-demand backups.
     */
    private boolean enabled = true;

    /**
     * Storage directory where backup archives and manifests are saved.
     */
    private String storageDirectory = "./backups";

    /**
     * Cron expression for scheduled automated backups (default: 2:00 AM daily).
     */
    private String scheduleCron = "0 0 2 * * *";

    /**
     * Whether backup archives are encrypted with AES-256.
     */
    private boolean encryptionEnabled = true;

    /**
     * Secret passphrase used for AES-256 backup encryption and key derivation.
     */
    private String encryptionSecret = "Ujuzi360SecureBackupPassphrase2026";

    /**
     * Maximum number of historical backup archives to retain before pruning.
     */
    private int retentionMaxCount = 7;
}
