package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.dto.AuthResponse;
import com.zone01kisumu.backend.dto.PasswordResetDTOs.ForgotPasswordRequest;
import com.zone01kisumu.backend.dto.PasswordResetDTOs.ResetPasswordRequest;
import com.zone01kisumu.backend.service.OtpService;
import com.zone01kisumu.backend.service.UserLookupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserLookupService userLookupService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/forgot")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            var userResult = userLookupService.findUserByPhoneOrEmail(request.getPhoneOrEmail());
            if (userResult.isEmpty()) {
                // Deliberately vague message: don't reveal whether an account exists
                // Add a fixed delay to reduce timing side-channels that reveal whether
                // an account exists (existing path sends an email/SMS which takes time).
                final long NOT_FOUND_RESPONSE_DELAY_MS = 1000L; // 1 second
                try {
                    Thread.sleep(NOT_FOUND_RESPONSE_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                return ResponseEntity.ok(AuthResponse.builder()
                        .message("If an account exists, a reset code has been sent.")
                        .build());
            }

            String otpMethod = userLookupService.isEmail(request.getPhoneOrEmail()) ? "email" : "sms";
            otpService.sendOtp(
                    request.getPhoneOrEmail(),
                    otpMethod,
                    com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET
            );

            return ResponseEntity.ok(AuthResponse.builder()
                    .message("If an account exists, a reset code has been sent.")
                    .build());
        } catch (Exception e) {
            log.error("Error initiating password reset: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder().message("Failed to process request").build());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            boolean isValidOtp = otpService.verifyOtp(
                    request.getPhoneOrEmail(),
                    request.getOtpCode(),
                    com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET
            );
            if (!isValidOtp) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder().message("Invalid or expired code").build());
            }

            var userResult = userLookupService.findUserByPhoneOrEmail(request.getPhoneOrEmail());
            if (userResult.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder().message("No account found").build());
            }

            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            userLookupService.updateUserPassword(userResult.get().user, encodedPassword);

            return ResponseEntity.ok(AuthResponse.builder()
                    .message("Password reset successful. You can now log in with your new password.")
                    .build());
        } catch (Exception e) {
            log.error("Error resetting password: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder().message("Failed to reset password").build());
        }
    }
}
