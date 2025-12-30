package com.example.hoccode.dto.payment;

import java.time.LocalDateTime;

public record PaidPaymentReportRow(
        int stt,
        String email,
        Long amount,
        String orderCode,
        LocalDateTime paidAt
) {}