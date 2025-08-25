package com.example.hoccode.service;

public interface IRateLimiterService {
    boolean isAllowed(String endpoint);
}
