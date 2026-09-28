package com.zone01kisumu.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendRequest {

    @NotBlank(message = "Phone or email is required")
    private String phoneOrEmail;

    @NotBlank(message = "Role is required")
    private String role; // "student", "teacher", "institution"

    private String otpMethod; // "sms" or "email"
}