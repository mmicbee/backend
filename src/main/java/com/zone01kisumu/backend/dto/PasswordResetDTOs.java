package com.zone01kisumu.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PasswordResetDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank
        private String phoneOrEmail;
        @NotBlank
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank
        private String phoneOrEmail;
        @NotBlank
        private String otpCode;
        @NotBlank
        @Size(min = 8, max = 255, message = "Password must be at least 8 characters long")
        private String newPassword;
        @NotBlank
        private String role;
    }
}
