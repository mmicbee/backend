package com.zone01kisumu.backend.securityTests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogMaskingSecurityTest {

    private PatternLayout layout;
    private LoggerContext context;

    @BeforeEach
    void setUp() {
        context = (LoggerContext) LoggerFactory.getILoggerFactory();
        layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %replace(%msg){'(?i)(sk_(?:live|test)_[a-zA-Z0-9]+|(?:Bearer|Basic)\\s+[A-Za-z0-9_\\-\\.\\+/=]+|(?:PAYSTACK_SECRET_KEY|MPESA_CONSUMER_SECRET|MPESA_PASSKEY|MPESA_SECURITY_CREDENTIAL|secretKey|appSecret|passkey)[=:][^\\s,]+)','[PROTECTED]'}%n");
        layout.start();
    }

    private String formatMessage(String rawMessage) {
        Logger logger = context.getLogger(LogMaskingSecurityTest.class);
        ILoggingEvent event = new LoggingEvent(
                Logger.class.getName(),
                logger,
                Level.INFO,
                rawMessage,
                null,
                null
        );
        return layout.doLayout(event);
    }

    @Test
    void testMaskingPaystackLiveSecretKey() {
        String secret = "sk_live_test123";
        String message = "Initializing payment with key: " + secret;
        String formatted = formatMessage(message);

        assertFalse(formatted.contains(secret), "Formatted log must not contain real secret key");
        assertTrue(formatted.contains("[PROTECTED]"), "Formatted log must contain [PROTECTED]");
    }

    @Test
    void testMaskingPaystackTestSecretKey() {
        String secret = "sk_test_9876543210abcdef";
        String message = "Paystack response using key=" + secret;
        String formatted = formatMessage(message);

        assertFalse(formatted.contains(secret), "Formatted log must not contain real secret key");
        assertTrue(formatted.contains("[PROTECTED]"), "Formatted log must contain [PROTECTED]");
    }

    @Test
    void testMaskingBearerToken() {
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String message = "Request headers: " + token;
        String formatted = formatMessage(message);

        assertFalse(formatted.contains(token), "Formatted log must not contain raw bearer token");
        assertTrue(formatted.contains("[PROTECTED]"), "Formatted log must contain [PROTECTED]");
    }

    @Test
    void testMaskingBasicAuth() {
        String auth = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";
        String message = "Sending auth header: " + auth;
        String formatted = formatMessage(message);

        assertFalse(formatted.contains(auth), "Formatted log must not contain basic auth token");
        assertTrue(formatted.contains("[PROTECTED]"), "Formatted log must contain [PROTECTED]");
    }

    @Test
    void testMaskingMpesaConsumerSecret() {
        String secretAssignment = "MPESA_CONSUMER_SECRET=superSecretMpesaKey123";
        String message = "Configuration loaded with " + secretAssignment;
        String formatted = formatMessage(message);

        assertFalse(formatted.contains("superSecretMpesaKey123"), "Formatted log must not contain mpesa secret");
        assertTrue(formatted.contains("[PROTECTED]"), "Formatted log must contain [PROTECTED]");
    }

    @Test
    void testMaskingMpesaPasskey() {
        String passkeyAssignment = "MPESA_PASSKEY:bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
        String message = "Setting STK push passkey: " + passkeyAssignment;
        String formatted = formatMessage(message);

        assertFalse(formatted.contains("bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"), "Formatted log must not contain passkey");
        assertTrue(formatted.contains("[PROTECTED]"), "Formatted log must contain [PROTECTED]");
    }

    @Test
    void testMaskingMpesaSecurityCredential() {
        String cred = "MPESA_SECURITY_CREDENTIAL=EncryptedB2CCredentialXYZ";
        String message = "B2C init: " + cred;
        String formatted = formatMessage(message);

        assertFalse(formatted.contains("EncryptedB2CCredentialXYZ"), "Formatted log must not contain security credential");
        assertTrue(formatted.contains("[PROTECTED]"), "Formatted log must contain [PROTECTED]");
    }

    @Test
    void testMaskingPropertyKeys() {
        String msg = "secretKey=myPaystackSecret and appSecret:myMpesaSecret and passkey=myPasskeyVal";
        String formatted = formatMessage(msg);

        assertFalse(formatted.contains("myPaystackSecret"));
        assertFalse(formatted.contains("myMpesaSecret"));
        assertFalse(formatted.contains("myPasskeyVal"));
        assertTrue(formatted.contains("[PROTECTED]"));
    }

    @Test
    void testNonSensitiveLogsAreNotMasked() {
        String normalMsg = "Payment initiated successfully for student 10 and course 5";
        String formatted = formatMessage(normalMsg);

        assertTrue(formatted.contains(normalMsg));
        assertFalse(formatted.contains("[PROTECTED]"));
    }
}
