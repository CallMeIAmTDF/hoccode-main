package com.example.hoccode.service.redis;

import com.example.hoccode.service.base.BaseRedisServiceV2;

public interface IStringStringIntegerRedis extends BaseRedisServiceV2<String, String, Integer> {
//    boolean isUserViewed(String uid, String pid);

    Integer getDailyChallenge();

    void setDailyChallenge(Integer i);
}
