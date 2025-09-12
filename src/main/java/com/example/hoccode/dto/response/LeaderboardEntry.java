package com.example.hoccode.dto.response;

public class LeaderboardEntry {
    private Long userId;
    private Integer rank;

    public LeaderboardEntry(Long userId, Integer rank) {
        this.userId = userId;
        this.rank = rank;
    }

    // Getters
    public Long getUserId() { return userId; }
    public Integer getRank() { return rank; }
}