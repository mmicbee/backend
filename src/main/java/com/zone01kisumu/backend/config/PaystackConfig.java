package com.zone01kisumu.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "paystack")
public class PaystackConfig {
    private String secretKey;
    private String publicKey;
    private String baseUrl;

}
