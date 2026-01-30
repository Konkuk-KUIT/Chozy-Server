package com.kuit.chozy.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRedisRepository {

    private final StringRedisTemplate redis;

    private String codeKey(String email) {
        return "email:code:" + email;
    }

    private String verifiedKey(String email) {
        return "email:verified:" + email;
    }

    public void saveCode(String email, String code, Duration ttl) {
        redis.opsForValue().set(codeKey(email), code, ttl);
    }

    public String getCode(String email) {
        return redis.opsForValue().get(codeKey(email));
    }

    public void deleteCode(String email) {
        redis.delete(codeKey(email));
    }

    public void markVerified(String email, Duration ttl) {
        redis.opsForValue().set(verifiedKey(email), "true", ttl);
    }

    public boolean isVerified(String email) {
        return "true".equals(redis.opsForValue().get(verifiedKey(email)));
    }

    public void clearVerified(String email) {
        redis.delete(verifiedKey(email));
    }
}
