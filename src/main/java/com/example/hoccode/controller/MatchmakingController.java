package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.response.CategoryCountResponse;
import com.example.hoccode.service.MatchmakingService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/combat")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchmakingController {
    MatchmakingService matchmakingService;
    @GetMapping("/join")
    public ResponseEntity<ApiResponse<String>> joinMatchmaking() {
        matchmakingService.joinMatchmaking();

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Joined matchmaking queue")
                .data("Joined matchmaking queue")
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<String>> submitCode(@RequestParam String roomId, @RequestBody CreateSubmissionRequest submissionRequest) {
        matchmakingService.handleSubmission(roomId, submissionRequest);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("handleSubmission")
                .data("handleSubmission")
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }
}