package dev.handsup.common.service;

import static java.lang.Boolean.*;

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

    private static final long TTL = 60 * 30L; // 30분
    private final StringRedisTemplate redisTemplate;

    public boolean checkDuplicateAndCacheIfAbsent(String eventId) {
        String key = KeyGenerator.generateBiddingEventKey(eventId);
        Boolean isNewKeyCreated = redisTemplate.opsForValue()
            .setIfAbsent("event:" + eventId, "1", Duration.ofSeconds(TTL));

        if (TRUE.equals(isNewKeyCreated)) {
            log.debug("Redis 키 생성 완료: key={}", key);
        } else {
            log.info("Redis 키 중복: key={}", key);
        }

        return FALSE.equals(isNewKeyCreated);
    }
}
