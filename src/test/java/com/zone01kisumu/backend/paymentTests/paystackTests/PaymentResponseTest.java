package com.zone01kisumu.backend.paymentTests.paystackTests;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.PaymentResponse;

import static org.junit.jupiter.api.Assertions.*;

class PaymentResponseTest {

    @Test
    void testConstructorAndGetters() {
        String authUrl = "https://paystack.com/authorize";
        String reference = "ref12345";
        String accessCode = "accessXYZ";

        PaymentResponse response = new PaymentResponse(authUrl, reference, accessCode);

        assertEquals(authUrl, response.getAuthorizationUrl());
        assertEquals(reference, response.getReference());
        assertEquals(accessCode, response.getAccessCode());
    }

    @Test
    void testSetters() {
        PaymentResponse response = new PaymentResponse(null, null, null);

        response.setAuthorizationUrl("https://new.url");
        response.setReference("newRef");
        response.setAccessCode("newAccessCode");

        assertEquals("https://new.url", response.getAuthorizationUrl());
        assertEquals("newRef", response.getReference());
        assertEquals("newAccessCode", response.getAccessCode());
    }

    @Test
    void testToStringContainsFields() {
        PaymentResponse response = new PaymentResponse("url", "ref", "code");
        String toString = response.toString();

        assertTrue(toString.contains("authorizationUrl=url"));
        assertTrue(toString.contains("reference=ref"));
        assertTrue(toString.contains("accessCode=code"));
    }

    @Test
    void testEqualsAndHashCode() {
        PaymentResponse r1 = new PaymentResponse("url", "ref", "code");
        PaymentResponse r2 = new PaymentResponse("url", "ref", "code");
        PaymentResponse r3 = new PaymentResponse("diff", "ref", "code");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        assertNotEquals(r1, r3);
        assertNotEquals(r1.hashCode(), r3.hashCode());
    }
}
