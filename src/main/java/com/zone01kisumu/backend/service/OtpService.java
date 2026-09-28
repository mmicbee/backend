package com.zone01kisumu.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final ConcurrentHashMap<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);


    public enum Purpose {
        LOGIN,
        PASSWORD_RESET
    }

    public static class OtpData {
        public final String otp;
        public final LocalDateTime expiryTime;
        public final String method;
        public final Purpose purpose;

        // Backwards-compatible constructor defaults to LOGIN purpose
        public OtpData(String otp, LocalDateTime expiryTime, String method) {
            this(otp, expiryTime, method, Purpose.LOGIN);
        }

        public OtpData(String otp, LocalDateTime expiryTime, String method, Purpose purpose) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.method = method;
            this.purpose = purpose;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    // Backwards-compatible sendOtp (defaults to LOGIN purpose)
    public void sendOtp(String phoneOrEmail, String method) {
        sendOtp(phoneOrEmail, method, Purpose.LOGIN);
    }

    // New API: specify purpose explicitly
    public void sendOtp(String phoneOrEmail, String method, Purpose purpose) {
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        otpStore.put(phoneOrEmail, new OtpData(otp, expiryTime, method, purpose));

        if (isEmail(phoneOrEmail)) {
            sendOtpByEmail(phoneOrEmail, otp);
        } else {
            sendOtpBySms(phoneOrEmail, otp);
        }

        log.info("OTP sent to {} via {}", phoneOrEmail, method);
    }

    // Backwards-compatible verify (defaults to LOGIN purpose)
    public boolean verifyOtp(String phoneOrEmail, String otpCode) {
        return verifyOtp(phoneOrEmail, otpCode, Purpose.LOGIN);
    }

    // New API: verify with purpose
    public boolean verifyOtp(String phoneOrEmail, String otpCode, Purpose purpose) {
        OtpData otpData = otpStore.get(phoneOrEmail);

        if (otpData == null) {
            log.warn("No OTP found for {}", phoneOrEmail);
            return false;
        }

        if (otpData.isExpired()) {
            otpStore.remove(phoneOrEmail);
            log.warn("OTP expired for {}", phoneOrEmail);
            return false;
        }

        // Ensure purpose matches
        if (otpData.purpose != purpose) {
            log.warn("OTP purpose mismatch for {}: expected={}, actual={}", phoneOrEmail, purpose, otpData.purpose);
            return false;
        }

        boolean isValid = otpData.otp.equals(otpCode);
        if (isValid) {
            otpStore.remove(phoneOrEmail); // Remove OTP after successful verification
            log.info("OTP verified successfully for {}", phoneOrEmail);
        } else {
            log.warn("Invalid OTP provided for {}", phoneOrEmail);
        }

        return isValid;
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    private boolean isEmail(String phoneOrEmail) {
        return EMAIL_PATTERN.matcher(phoneOrEmail).matches();
    }

    private void sendOtpByEmail(String email, String otp) {
        String subject = "Your OTP for Login";
        String body = String.format(
            "Your OTP for login is: %s\n\n" +
            "This OTP will expire in %d minutes.\n" +
            "Do not share this OTP with anyone.",
            otp, OTP_EXPIRY_MINUTES
        );

        emailService.sendOtpEmail(email, subject, body);
    }

    private void sendOtpBySms(String phone, String otp) {
        // TODO: Implement SMS sending using Twilio, AWS SNS, or similar service
        // For now, just log the OTP (remove this in production)
        log.info("SMS OTP for {}: {} (This should be sent via SMS service in production)", phone, otp);

        // Example implementation with Twilio would look like:
        // twilioService.sendSms(phone, "Your OTP is: " + otp);
    }

    public void cleanupExpiredOtps() {
        otpStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}