package dev.handsup.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.common.dto.PageResponse;

@TestConfiguration
@EnableCaching
public class TestCacheConfig {

    @Bean
    public Cache<String, PageResponse<RecommendAuctionResponse>> testAuctionCache() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }
}
