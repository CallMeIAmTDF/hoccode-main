package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.response.RoomValidationResponse;
import com.example.hoccode.dto.response.VideoRoomResponse;
import com.example.hoccode.service.IInterviewService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interview")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InterviewController {

    IInterviewService interviewService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<VideoRoomResponse>> createRoom() {
        ApiResponse<VideoRoomResponse> apiResponse = ApiResponse.<VideoRoomResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Create Interview Room")
                .data(interviewService.createRoom())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomValidationResponse>> validateRoom(@PathVariable String roomId) {
        ApiResponse<RoomValidationResponse> apiResponse = ApiResponse.<RoomValidationResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Validate Room")
                .data(interviewService.validateRoom(roomId))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
