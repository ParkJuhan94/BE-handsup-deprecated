package dev.handsup.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.common.dto.PageResponse;

@TestConfiguration
@EnableCaching
public class TestRedisConfig {

    @Bean
    public RedisTemplate<String, PageResponse<RecommendAuctionResponse>> recommendAuctionRedisTemplate(
        RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PageResponse<RecommendAuctionResponse>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

}
