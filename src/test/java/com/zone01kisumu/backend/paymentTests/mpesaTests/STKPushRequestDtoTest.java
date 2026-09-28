package com.zone01kisumu.backend.paymentTests.mpesaTests;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.STKPushRequestDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class STKPushRequestDtoTest {

    @Test
    void testFieldsAndAccessors() {

        Long expectedStudentId = 1L;
        Long expectedCourseId = 99L;
        BigDecimal expectedAmount = new BigDecimal("500.00");
        String expectedPhoneNumber = "254712345678";
        String expectedTransactionType = "CustomerPayBillOnline";

        STKPushRequestDto dto = new STKPushRequestDto();

        dto.setStudentId(expectedStudentId);
        dto.setCourseId(expectedCourseId);
        dto.setAmount(expectedAmount);
        dto.setPhoneNumber(expectedPhoneNumber);
        dto.setTransactionType(expectedTransactionType);

        assertEquals(expectedStudentId, dto.getStudentId());
        assertEquals(expectedCourseId, dto.getCourseId());
        assertEquals(expectedAmount, dto.getAmount());
        assertEquals(expectedPhoneNumber, dto.getPhoneNumber());
        assertEquals(expectedTransactionType, dto.getTransactionType());
    }

    @Test
    void testDefaultConstructor() {
        STKPushRequestDto dto = new STKPushRequestDto();
        assertNotNull(dto);
    }

    @Test
    void testToStringContainsFieldValues() {
        STKPushRequestDto dto = new STKPushRequestDto();
        dto.setStudentId(2L);
        dto.setCourseId(10L);
        dto.setAmount(new BigDecimal("1200"));
        dto.setPhoneNumber("254700000000");
        dto.setTransactionType("PayBill");

        String result = dto.toString();

        assertTrue(result.contains("254700000000"));
        assertTrue(result.contains("1200"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("PayBill"));
    }
}
