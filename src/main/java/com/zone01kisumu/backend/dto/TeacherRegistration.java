package com.zone01kisumu.backend.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;     // For minimum numeric value validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;    // For non-null validation on non-string types
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TeacherRegistration {
    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name cannot exceed 255 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name cannot exceed 255 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters long")
    private String password;

    // Teacher-specific fields with validation
    @Size(max = 255, message = "Professional level cannot exceed 255 characters")
    private String professionalLevel;

    @Size(max = 255, message = "Certification cannot exceed 255 characters")
    // Certification can be null/blank if not available, so no @NotBlank here
    private String certification;

    @Size(max = 255, message = "Profile picture URL cannot exceed 255 characters")
    // Profile picture can be null/blank
    private String profilePicture;

    @Size(max = 1000, message = "Biography cannot exceed 1000 characters") // Example max length for bio (TEXT in DB)
    private String bio;

    @Min(value = 0, message = "Years of experience cannot be negative") // Ensures experience is 0 or more
    private Integer yearOfExperience;

    @Size(max = 255, message = "Course cannot exceed 255 characters")
    private String course;

    @Size(max = 50, message = "Language cannot exceed 50 characters")
    private String language;
    
}
