package dev.handsup.common.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import dev.handsup.support.TestContainerSupport;

@DisplayName("[Redis Duplicate Checker 테스트]")
@DataRedisTest
class RedisDuplicateCheckerTest extends TestContainerSupport {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisConnectionFactory connectionFactory;

    @DisplayName("[setIfAbsent()가 처음 key는 set & return true, 두 번째 key는 return false 한다.]")
    @Test
    void keySetAndCheck() {
        String key = "bidding:event:test-key:" + UUID.randomUUID();
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(5));
        assertTrue(first); // 최초는 true

        Boolean second = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(5));
        assertFalse(second); // 중복 시 false
    }

    @Test
    void printRedisInfo() {
        RedisStandaloneConfiguration config = (RedisStandaloneConfiguration)
            ((LettuceConnectionFactory) connectionFactory).getStandaloneConfiguration();

        System.out.println("✅ Redis Host = " + config.getHostName());
        System.out.println("✅ Redis Port = " + config.getPort());
    }
}