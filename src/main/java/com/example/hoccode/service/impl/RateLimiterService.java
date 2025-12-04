package com.example.hoccode.service.impl;

import com.example.hoccode.service.IRateLimiterService;
import com.example.hoccode.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimiterService implements IRateLimiterService {
    RedisTemplate<String, String> redisTemplate;
    SecurityUtil securityUtil;

    private static final long RATE_LIMIT = 5;
    private static final long WINDOW_SECONDS = 60;

    private static final Map<String, long[]> CUSTOM_RATE_LIMITS = Map.of(
            "submission-post-limit", new long[]{1, 30}, // POST /submission: 1 request / 30 giây
            "chat-post-limit", new long[]{10, 5}
    );

    private long[] getRateLimitConfig(String endpoint) {
        if (CUSTOM_RATE_LIMITS.containsKey(endpoint)) {
            return CUSTOM_RATE_LIMITS.get(endpoint);
        }
        return new long[]{RATE_LIMIT, WINDOW_SECONDS};
    }

    @Override
    public boolean isAllowed(String endpoint) {
        long[] config = getRateLimitConfig(endpoint);
        final long limit = config[0];
        final long windowSeconds = config[1];

        Optional<String> userIdOpt = securityUtil.getCurrentUserLogin();
        if (userIdOpt.isEmpty()) {
            return false;
        }

        String userEmail = userIdOpt.get();
        String key = "ratelimit:" + endpoint + ":" + userEmail;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return true;
        }
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return count <= limit;
    }
}