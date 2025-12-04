package com.example.hoccode.service.redis;

import java.util.Set;

public interface IInterviewRedis {
    void saveRoom(String roomId);

    boolean isRoomExist(String roomId);

    void addUserToRoom(String roomId, String userEmail);

    void removeUserFromRoom(String roomId, String userEmail);

    boolean isUserInRoom(String roomId, String userEmail);

    Set<String> getRoomParticipants(String roomId);

    void deleteRoom(String roomId);

    void setRoomExpiry(String roomId, long seconds);
}
