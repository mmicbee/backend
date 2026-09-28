package com.zone01kisumu.backend.paymentTests.paystackTests;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.PaystackInitializeResponse;

import static org.junit.jupiter.api.Assertions.*;

class PaystackInitializeResponseTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        PaystackInitializeResponse.Data data = new PaystackInitializeResponse.Data();
        data.setAuthorizationUrl("https://paystack.com/authorize");
        data.setAccessCode("access123");
        data.setReference("ref123");

        PaystackInitializeResponse response = new PaystackInitializeResponse(true, "Payment initialized", data);

        assertTrue(response.isStatus());
        assertEquals("Payment initialized", response.getMessage());
        assertEquals("https://paystack.com/authorize", response.getData().getAuthorizationUrl());
        assertEquals("access123", response.getData().getAccessCode());
        assertEquals("ref123", response.getData().getReference());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        PaystackInitializeResponse.Data data = new PaystackInitializeResponse.Data();
        data.setAuthorizationUrl("https://example.com/auth");
        data.setAccessCode("codeXYZ");
        data.setReference("ref456");

        PaystackInitializeResponse response = new PaystackInitializeResponse();
        response.setStatus(false);
        response.setMessage("Failed to initialize");
        response.setData(data);

        assertFalse(response.isStatus());
        assertEquals("Failed to initialize", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("https://example.com/auth", response.getData().getAuthorizationUrl());
        assertEquals("codeXYZ", response.getData().getAccessCode());
        assertEquals("ref456", response.getData().getReference());
    }

    @Test
    void testNestedDataToStringAndEquality() {
        PaystackInitializeResponse.Data data1 = new PaystackInitializeResponse.Data();
        data1.setAuthorizationUrl("url");
        data1.setAccessCode("access");
        data1.setReference("ref");

        PaystackInitializeResponse.Data data2 = new PaystackInitializeResponse.Data();
        data2.setAuthorizationUrl("url");
        data2.setAccessCode("access");
        data2.setReference("ref");

        assertEquals(data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
        assertTrue(data1.toString().contains("authorizationUrl=url"));
    }
}
