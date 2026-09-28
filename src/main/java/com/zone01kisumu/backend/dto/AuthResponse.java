package com.zone01kisumu.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuthResponse {
    private String message;
    private String token; 
    private String userId;// The ID of the logged-in user (Learner/Teacher ID)
}
