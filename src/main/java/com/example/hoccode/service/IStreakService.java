package com.example.hoccode.service;

import com.example.hoccode.entity.Streak;

public interface IStreakService {
    void increment(String email);
    Streak getStreak(String email);
}
