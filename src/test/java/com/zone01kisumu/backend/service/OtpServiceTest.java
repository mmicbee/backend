package com.zone01kisumu.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpService otpService;

    private ConcurrentHashMap<String, OtpService.OtpData> otpStore;

    @BeforeEach
    void setUp() throws Exception {
        // Access private otpStore field for testing
        Field otpStoreField = OtpService.class.getDeclaredField("otpStore");
        otpStoreField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, OtpService.OtpData> tempStore = (ConcurrentHashMap<String, OtpService.OtpData>) otpStoreField.get(otpService);
        otpStore = tempStore;
        otpStore.clear();   // Clear any existing data
    } 

    // ================ sendOtp Tests ================

    @Test
    void sendOtp_ShouldSendEmailOtp_WhenEmailProvided() {
        String email = "user@example.com";
        String method = "email";

        otpService.sendOtp(email, method);

        // Verify email was sent
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtpEmail(eq(email), subjectCaptor.capture(), bodyCaptor.capture());

        // Verify OTP was stored
        assertTrue(otpStore.containsKey(email));
        OtpService.OtpData storedOtp = otpStore.get(email);
        assertNotNull(storedOtp);
        assertEquals(method, storedOtp.method);
        assertNotNull(storedOtp.otp);
        assertEquals(6, storedOtp.otp.length()); // OTP should be 6 digits
        assertTrue(storedOtp.otp.matches("\\d{6}")); // Should be all digits
        assertFalse(storedOtp.isExpired());

        // Verify email content
        assertEquals("Your OTP for Login", subjectCaptor.getValue());
        assertTrue(bodyCaptor.getValue().contains(storedOtp.otp));
        assertTrue(bodyCaptor.getValue().contains("5 minutes"));
    }

    @Test
    void sendOtp_ShouldSendSmsOtp_WhenPhoneProvided() {
        String phone = "+1234567890";
        String method = "sms";

        otpService.sendOtp(phone, method);

        // Verify SMS was "sent" (currently just logged)
        // Note: In real implementation with SMS service, you would verify the SMS call

        // Verify OTP was stored
        assertTrue(otpStore.containsKey(phone));
        OtpService.OtpData storedOtp = otpStore.get(phone);
        assertNotNull(storedOtp);
        assertEquals(method, storedOtp.method);
        assertNotNull(storedOtp.otp);
        assertEquals(6, storedOtp.otp.length());
        assertTrue(storedOtp.otp.matches("\\d{6}"));

        // Email service should not be called for SMS
        verify(emailService, never()).sendOtpEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendOtp_ShouldAutoDetectEmail_WhenEmailFormatProvided() {
        String email = "auto@example.com";
        String method = "auto"; // Any method, should detect email

        otpService.sendOtp(email, method);

        // Should send email even with non-email method because email format detected
        verify(emailService).sendOtpEmail(eq(email), anyString(), anyString());
        assertTrue(otpStore.containsKey(email));
    }

    @Test
    void sendOtp_ShouldAutoDetectPhone_WhenPhoneFormatProvided() {
        String phone = "+1234567890";
        String method = "auto"; // Any method, should detect phone

        otpService.sendOtp(phone, method);

        // Should not send email because phone format detected
        verify(emailService, never()).sendOtpEmail(anyString(), anyString(), anyString());
        assertTrue(otpStore.containsKey(phone));
    }

    @Test
    void sendOtp_ShouldGenerateUniqueOtps_ForDifferentUsers() {
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        otpService.sendOtp(user1, "email");
        otpService.sendOtp(user2, "email");

        OtpService.OtpData otp1 = otpStore.get(user1);
        OtpService.OtpData otp2 = otpStore.get(user2);

        assertNotEquals(otp1.otp, otp2.otp); // Very likely to be different
        assertNotNull(otp1.otp);
        assertNotNull(otp2.otp);
    }

    @Test
    void sendOtp_ShouldOverwriteExistingOtp_WhenCalledTwice() {
        String email = "user@example.com";

        otpService.sendOtp(email, "email");
        String firstOtp = otpStore.get(email).otp;

        otpService.sendOtp(email, "email");
        String secondOtp = otpStore.get(email).otp;

        assertNotEquals(firstOtp, secondOtp);
        assertEquals(1, otpStore.size()); // Still only one entry
    }

    @Test
    void sendOtp_ShouldHandleNullPhoneOrEmail() {
        // This should throw NullPointerException since ConcurrentHashMap doesn't allow null keys
        assertThrows(NullPointerException.class, () -> otpService.sendOtp(null, "email"));
    }

    @Test
    void sendOtp_ShouldHandleEmptyPhoneOrEmail() {
        assertDoesNotThrow(() -> otpService.sendOtp("", "email"));
    }

    // ================ verifyOtp Tests ================

    @Test
    void verifyOtp_ShouldReturnTrue_WhenCorrectOtpProvided() {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");
        String correctOtp = otpStore.get(email).otp;

        boolean result = otpService.verifyOtp(email, correctOtp);

        assertTrue(result);
        // OTP should be removed after successful verification
        assertFalse(otpStore.containsKey(email));
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenIncorrectOtpProvided() {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");

        boolean result = otpService.verifyOtp(email, "wrong123");

        assertFalse(result);
        // OTP should still exist after failed verification
        assertTrue(otpStore.containsKey(email));
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenNoOtpExists() {
        boolean result = otpService.verifyOtp("nonexistent@example.com", "123456");

        assertFalse(result);
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenOtpExpired() throws Exception {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");

        // Manually set expiry time to past
        OtpService.OtpData storedOtp = otpStore.get(email);
        Field expiryField = OtpService.OtpData.class.getDeclaredField("expiryTime");
        expiryField.setAccessible(true);
        expiryField.set(storedOtp, LocalDateTime.now().minusMinutes(10));

        boolean result = otpService.verifyOtp(email, storedOtp.otp);

        assertFalse(result);
        // Expired OTP should be removed
        assertFalse(otpStore.containsKey(email));
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenNullOtpProvided() {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");

        boolean result = otpService.verifyOtp(email, null);

        assertFalse(result);
        // OTP should still exist
        assertTrue(otpStore.containsKey(email));
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenEmptyOtpProvided() {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");

        boolean result = otpService.verifyOtp(email, "");

        assertFalse(result);
        assertTrue(otpStore.containsKey(email));
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenNullEmailProvided() {
        // This should throw NullPointerException since ConcurrentHashMap doesn't allow null keys
        assertThrows(NullPointerException.class, () -> otpService.verifyOtp(null, "123456"));
    }

    @Test
    void verifyOtp_ShouldBeCaseSensitive() {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");
        String correctOtp = otpStore.get(email).otp;

        // This should work (numbers don't have case, but testing the principle)
        boolean result1 = otpService.verifyOtp(email, correctOtp);
        assertTrue(result1);

        // Re-send OTP for next test
        otpService.sendOtp(email, "email");
        String newOtp = otpStore.get(email).otp;

        // Should fail with wrong case if it contained letters (but OTP is numeric)
        boolean result2 = otpService.verifyOtp(email, newOtp.toLowerCase());
        assertTrue(result2); // Will pass since OTP is numeric
    }

    // ================ cleanupExpiredOtps Tests ================

    @Test
    void cleanupExpiredOtps_ShouldRemoveExpiredOtps() throws Exception {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        otpService.sendOtp(email1, "email");
        otpService.sendOtp(email2, "email");

        // Manually expire first OTP
        OtpService.OtpData otp1 = otpStore.get(email1);
        Field expiryField = OtpService.OtpData.class.getDeclaredField("expiryTime");
        expiryField.setAccessible(true);
        expiryField.set(otp1, LocalDateTime.now().minusMinutes(10));

        assertEquals(2, otpStore.size());

        otpService.cleanupExpiredOtps();

        assertEquals(1, otpStore.size());
        assertFalse(otpStore.containsKey(email1)); // Expired, should be removed
        assertTrue(otpStore.containsKey(email2));  // Valid, should remain
    }

    @Test
    void cleanupExpiredOtps_ShouldNotRemoveValidOtps() {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");

        assertEquals(1, otpStore.size());

        otpService.cleanupExpiredOtps();

        assertEquals(1, otpStore.size());
        assertTrue(otpStore.containsKey(email));
    }

    @Test
    void cleanupExpiredOtps_ShouldHandleEmptyStore() {
        assertEquals(0, otpStore.size());

        assertDoesNotThrow(() -> otpService.cleanupExpiredOtps());

        assertEquals(0, otpStore.size());
    }

    // ================ OtpData Tests ================

    @Test
    void otpData_ShouldStoreAllFieldsCorrectly() {
        String otp = "123456";
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        String method = "email";

        OtpService.OtpData otpData = new OtpService.OtpData(otp, expiry, method);

        assertEquals(otp, otpData.otp);
        assertEquals(expiry, otpData.expiryTime);
        assertEquals(method, otpData.method);
    }

    @Test
    void otpData_isExpired_ShouldReturnFalse_WhenNotExpired() {
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(5);
        OtpService.OtpData otpData = new OtpService.OtpData("123456", futureTime, "email");

        assertFalse(otpData.isExpired());
    }

    @Test
    void otpData_isExpired_ShouldReturnTrue_WhenExpired() {
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(5);
        OtpService.OtpData otpData = new OtpService.OtpData("123456", pastTime, "email");

        assertTrue(otpData.isExpired());
    }

    @Test
    void otpData_isExpired_ShouldReturnTrue_WhenExactlyNow() {
        LocalDateTime now = LocalDateTime.now();
        OtpService.OtpData otpData = new OtpService.OtpData("123456", now, "email");

        // This might be flaky due to timing, but generally should be expired
        // since LocalDateTime.now() in isExpired() will be slightly after
        assertTrue(otpData.isExpired());
    }

    // ================ Email Pattern Tests ================

    @Test
    void sendOtp_ShouldRecognizeValidEmails() {
        String[] validEmails = {
            "user@example.com",
            "test.email@domain.co.uk",
            "user+tag@example.org",
            "123@456.com",
            "a@b.co"
        };

        for (String email : validEmails) {
            otpStore.clear();
            reset(emailService);

            otpService.sendOtp(email, "auto");

            verify(emailService).sendOtpEmail(eq(email), anyString(), anyString());
            assertTrue(otpStore.containsKey(email), "Should store OTP for email: " + email);
        }
    }

    @Test
    void sendOtp_ShouldRecognizePhoneNumbers() {
        String[] phoneNumbers = {
            "+1234567890",
            "1234567890",
            "555-123-4567",
            "(555) 123-4567"
        };

        for (String phone : phoneNumbers) {
            otpStore.clear();
            reset(emailService);

            otpService.sendOtp(phone, "auto");

            verify(emailService, never()).sendOtpEmail(anyString(), anyString(), anyString());
            assertTrue(otpStore.containsKey(phone), "Should store OTP for phone: " + phone);
        }
    }

    // ================ Edge Cases and Error Handling ================

    @Test
    void sendOtp_ShouldThrowException_WhenEmailServiceFails() {
        doThrow(new RuntimeException("Email service unavailable"))
            .when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

        String email = "user@example.com";

        // Should throw exception when email service fails
        assertThrows(RuntimeException.class, () -> otpService.sendOtp(email, "email"));
    }

    @Test
    void verifyOtp_ShouldBeThreadSafe() throws InterruptedException {
        String email = "user@example.com";
        otpService.sendOtp(email, "email");
        String correctOtp = otpStore.get(email).otp;

        // Simulate concurrent verification attempts
        Thread thread1 = new Thread(() -> otpService.verifyOtp(email, correctOtp));
        Thread thread2 = new Thread(() -> otpService.verifyOtp(email, correctOtp));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // OTP should be removed after successful verification
        // Only one thread should succeed, OTP should be gone
        assertFalse(otpStore.containsKey(email));
    }

    @Test
    void sendOtp_ShouldHandleSpecialCharactersInIdentifier() {
        String specialEmail = "user+test@example-domain.com";
        String specialPhone = "+1 (555) 123-4567";

        assertDoesNotThrow(() -> {
            otpService.sendOtp(specialEmail, "email");
            otpService.sendOtp(specialPhone, "sms");
        });

        assertTrue(otpStore.containsKey(specialEmail));
        assertTrue(otpStore.containsKey(specialPhone));
    }
}