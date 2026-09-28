package com.zone01kisumu.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for student profile viewing, updating, picture upload, and password change.
 */
public class StudentProfileDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationPreferencesDTO {
        @Builder.Default
        private Boolean courseUpdates = true;

        @Builder.Default
        private Boolean announcements = true;

        @Builder.Default
        private Boolean marketingEmails = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProfileResponse {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private String bio;
        private String profilePicture;
        private NotificationPreferencesDTO notificationPreferences;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStudentProfileRequest {
        private String firstName;
        private String lastName;
        private String fullName;

        @Email(message = "Invalid email format")
        private String email;

        private String phone;
        private String bio;
        private String profilePicture;
        private NotificationPreferencesDTO notificationPreferences;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "currentPassword is required")
        private String currentPassword;

        @NotBlank(message = "newPassword is required")
        @Size(min = 6, message = "newPassword must be at least 6 characters")
        private String newPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfilePictureResponse {
        private String profilePicture;
        private String message;
    }
}
