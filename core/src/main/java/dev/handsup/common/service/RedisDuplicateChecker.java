package dev.handsup.common.service;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import dev.handsup.common.util.KeyGenerator;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisDuplicateChecker {

    private static final long TTL = 60 * 5L; // 5분
    private final StringRedisTemplate redisTemplate;

    public boolean isDuplicate(String eventId) {
        String key = KeyGenerator.generateBiddingEventKey(eventId);
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent("event:" + eventId, "1", Duration.ofSeconds(TTL));

        if (Boolean.TRUE.equals(success)) {
            log.debug("✅ Redis key created: {}", key);
        } else {
            log.debug("🚨 Redis key already exists (duplicate): {}", key);
        }

        return success == Boolean.FALSE; // 이미 있으면 true
    }
}
