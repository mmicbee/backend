package com.zone01kisumu.backend.configTests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zone01kisumu.backend.config.ApplicationConfig;

class ApplicationConfigTest {

    private final ApplicationConfig config = new ApplicationConfig();

    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        String rawPassword = "secret123";
        String encodedPassword = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }
}
