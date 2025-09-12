package com.example.hoccode.controller;

import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.service.MatchmakingService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchmakingController {
    MatchmakingService matchmakingService;
    @PostMapping("/join")
    public String joinMatchmaking() {
        matchmakingService.joinMatchmaking();
        return "Joined matchmaking queue";
    }

    @PostMapping("/submit")
    public String submitCode(@RequestParam String roomId, @RequestBody CreateSubmissionRequest submissionRequest) {
        matchmakingService.handleSubmission(roomId, submissionRequest);
        return "Submission processed";
    }
}