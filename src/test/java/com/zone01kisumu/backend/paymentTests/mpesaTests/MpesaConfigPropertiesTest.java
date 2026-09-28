package com.zone01kisumu.backend.paymentTests.mpesaTests;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.config.MpesaConfigProperties;

import static org.junit.jupiter.api.Assertions.*;

class MpesaConfigPropertiesTest {

    @Test
    void testFieldSettersAndGetters() {
        MpesaConfigProperties config = new MpesaConfigProperties();

        config.setAppKey("testAppKey");
        config.setAppSecret("testAppSecret");
        config.setPasskey("testPasskey");
        config.setCallbackUrl("https://callback.example.com");
        config.setTimeoutUrl("https://timeout.example.com");
        config.setEnv("sandbox");

        assertEquals("testAppKey", config.getAppKey());
        assertEquals("testAppSecret", config.getAppSecret());
        assertEquals("testPasskey", config.getPasskey());
        assertEquals("https://callback.example.com", config.getCallbackUrl());
        assertEquals("https://timeout.example.com", config.getTimeoutUrl());
        assertEquals("sandbox", config.getEnv());
    }

    @Test
    void testToStringIsNotNull() {
        MpesaConfigProperties config = new MpesaConfigProperties();
        config.setAppKey("key");
        assertNotNull(config.toString());
    }

    @Test
    void testDefaultValues() {
        MpesaConfigProperties config = new MpesaConfigProperties();
        assertNull(config.getAppKey());
        assertNull(config.getAppSecret());
        assertNull(config.getPasskey());
        assertNull(config.getCallbackUrl());
        assertNull(config.getTimeoutUrl());
        assertNull(config.getEnv());
    }
}

