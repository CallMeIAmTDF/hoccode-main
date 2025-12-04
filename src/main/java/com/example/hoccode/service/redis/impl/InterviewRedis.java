package com.example.hoccode.service.redis.impl;

import com.example.hoccode.service.redis.IInterviewRedis;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InterviewRedis implements IInterviewRedis {
    RedisTemplate<String, String> redisTemplate;
    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String ROOM_PARTICIPANTS_PREFIX = "room:participants:";
    private static final long DEFAULT_ROOM_EXPIRY = 24 * 60 * 60;

    @Override
    public void saveRoom(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        redisTemplate.opsForValue().set(key, "active");
        redisTemplate.expire(key, DEFAULT_ROOM_EXPIRY, TimeUnit.SECONDS);
    }

    @Override
    public boolean isRoomExist(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        return redisTemplate.hasKey(key);
    }

    @Override
    public void addUserToRoom(String roomId, String userEmail) {
        String key = ROOM_PARTICIPANTS_PREFIX + roomId;
        redisTemplate.opsForSet().add(key, userEmail);

        // Extend expiry khi có user join
        redisTemplate.expire(key, DEFAULT_ROOM_EXPIRY, TimeUnit.SECONDS);
        redisTemplate.expire(ROOM_KEY_PREFIX + roomId, DEFAULT_ROOM_EXPIRY, TimeUnit.SECONDS);
    }

    @Override
    public void removeUserFromRoom(String roomId, String userEmail) {
        String key = ROOM_PARTICIPANTS_PREFIX + roomId;
        redisTemplate.opsForSet().remove(key, userEmail);
    }

    @Override
    public boolean isUserInRoom(String roomId, String userEmail) {
        String key = ROOM_PARTICIPANTS_PREFIX + roomId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userEmail));
    }

    @Override
    public Set<String> getRoomParticipants(String roomId) {
        String key = ROOM_PARTICIPANTS_PREFIX + roomId;
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public void deleteRoom(String roomId) {
        String roomKey = ROOM_KEY_PREFIX + roomId;
        String participantsKey = ROOM_PARTICIPANTS_PREFIX + roomId;

        redisTemplate.delete(roomKey);
        redisTemplate.delete(participantsKey);
    }

    @Override
    public void setRoomExpiry(String roomId, long seconds) {
        String roomKey = ROOM_KEY_PREFIX + roomId;
        String participantsKey = ROOM_PARTICIPANTS_PREFIX + roomId;
        redisTemplate.expire(roomKey, seconds, TimeUnit.SECONDS);
        redisTemplate.expire(participantsKey, seconds, TimeUnit.SECONDS);
    }
}
