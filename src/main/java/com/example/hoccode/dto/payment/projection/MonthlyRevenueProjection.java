package com.example.hoccode.dto.payment.projection;

public interface MonthlyRevenueProjection {
    String getMonth();   // alias phải trùng
    Long getRevenue();
}