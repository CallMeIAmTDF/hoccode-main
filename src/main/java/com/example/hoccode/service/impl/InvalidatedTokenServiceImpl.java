package com.example.hoccode.service.impl;

import com.example.hoccode.dto.request.InvalidatedTokenRequest;
import com.example.hoccode.service.InvalidatedTokenService;
import com.example.hoccode.service.base.impl.BaseRedisServiceImplV2;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvalidatedTokenServiceImpl extends BaseRedisServiceImplV2<String, String, String> implements InvalidatedTokenService {

    public InvalidatedTokenServiceImpl(RedisTemplate<String, String> redisTemplate, HashOperations<String, String, String> hashOperations) {
        super(redisTemplate, hashOperations);
    }

    /**
     * @param invalidatedTokenRequest - Input InvalidatedTokenRequest Object
     */
    @Override
    public void createInvalidatedToken(InvalidatedTokenRequest invalidatedTokenRequest) {
        this.set(invalidatedTokenRequest.getId(), "");
        this.setTimeToLive(invalidatedTokenRequest.getId(),
                invalidatedTokenRequest.getExpiryTime().getEpochSecond() - Instant.now().getEpochSecond());
    }

    /**
     * @param id - Input invalidatedTokenId
     * @return boolean indicating if the id already exited or not
     */
    @Override
    public boolean existById(String id){
        return this.hashExist(id);
    }
}