package com.example.hoccode.service;

import com.example.hoccode.dto.response.RoomValidationResponse;
import com.example.hoccode.dto.response.VideoRoomResponse;

public interface IInterviewService {
    VideoRoomResponse createRoom();

    RoomValidationResponse validateRoom(String roomId);
}
