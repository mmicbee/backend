package com.zone01kisumu.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class STKPushRequestDto {
    // DTO classes
    private Long studentId;
    private Long courseId;
    private BigDecimal amount;
    private String phoneNumber;
    private String transactionType;
}