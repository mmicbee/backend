package com.zone01kisumu.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaystackInitializeResponse {
    private boolean status;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        @JsonProperty("authorization_url")
        private String authorizationUrl;

        @JsonProperty("access_code")
        private String accessCode;

        private String reference;
        // Getters and setters
        public String getAuthorizationUrl() { return authorizationUrl; }
        public void setAuthorizationUrl(String authorizationUrl) { this.authorizationUrl = authorizationUrl; }
        
        public String getAccessCode() { return accessCode; }
        public void setAccessCode(String accessCode) { this.accessCode = accessCode; }
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
    
}