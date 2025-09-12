package com.example.hoccode.service;

import com.example.hoccode.dto.request.create.CreateSubmissionRequest;

public interface MatchmakingService {
    void joinMatchmaking();
    void handleSubmission(String roomId, CreateSubmissionRequest submissionRequest);
    void updateLeaderboard(String roomId);
    void checkLobbyTimeouts();
    void checkRoomTimeouts();
}
