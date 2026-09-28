package com.zone01kisumu.backend.paymentTests.paystackTests;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.config.PaystackConfig;

import static org.junit.jupiter.api.Assertions.*;

class PaystackConfigTest {

    @Test
    void testSettersAndGetters() {
        PaystackConfig config = new PaystackConfig();

        String secretKey = "sk_test_123";
        String publicKey = "pk_test_456";
        String baseUrl = "https://api.paystack.co";

        config.setSecretKey(secretKey);
        config.setPublicKey(publicKey);
        config.setBaseUrl(baseUrl);

        assertEquals(secretKey, config.getSecretKey());
        assertEquals(publicKey, config.getPublicKey());
        assertEquals(baseUrl, config.getBaseUrl());
    }

    @Test
    void testToStringAndEquality() {
        PaystackConfig config1 = new PaystackConfig();
        config1.setSecretKey("sk");
        config1.setPublicKey("pk");
        config1.setBaseUrl("url");

        PaystackConfig config2 = new PaystackConfig();
        config2.setSecretKey("sk");
        config2.setPublicKey("pk");
        config2.setBaseUrl("url");

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertTrue(config1.toString().contains("secretKey=sk"));
    }
}

