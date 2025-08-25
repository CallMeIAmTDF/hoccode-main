package com.example.hoccode.service.redis.impl;

import com.example.hoccode.service.base.BaseRedisServiceV2;
import com.example.hoccode.service.base.impl.BaseRedisServiceImplV2;
import com.example.hoccode.service.redis.IStringStringIntegerRedis;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StringStringIntegerRedis extends BaseRedisServiceImplV2<String, String, Integer> implements IStringStringIntegerRedis {
    public StringStringIntegerRedis(RedisTemplate<String, Integer> redisTemplate, HashOperations<String, String, Integer> hashOperations) {
        super(redisTemplate, hashOperations);
    }
    @Override
    public Integer getDailyChallenge(){
        return this.hashGet("daily", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }
    @Override
    public void setDailyChallenge(Integer i) {
        System.out.println("KEY : daily:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        this.hashSet("daily", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), i);
    }

}
