package com.zone01kisumu.backend.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String authorizationUrl;
    private String reference;
    private String accessCode;

      public PaymentResponse(String authorizationUrl, String reference, String accessCode) {
        this.authorizationUrl = authorizationUrl;
        this.reference = reference;
        this.accessCode = accessCode;
    }
}
