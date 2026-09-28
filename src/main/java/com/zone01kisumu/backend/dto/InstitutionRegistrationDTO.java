package com.zone01kisumu.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionRegistrationDTO {

    private Long id;

    @NotBlank(message = "School name is required")
    private String institutionName;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "School type is required")
    private String schoolType;

    @NotBlank(message = "Education system is required")
    private String educationSystem;

    @NotBlank(message = "Location is required")
    private String location;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;

}
