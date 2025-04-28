package dev.handsup.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.common.dto.PageResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheMonitorScheduler {

    private final Cache<String, PageResponse<RecommendAuctionResponse>> recommendAuctionsCache;

    @Scheduled(fixedDelay = 10000)
    public void logRecommendAuctionsCacheStats() {
        CacheStats stats = recommendAuctionsCache.stats();

        long hitCount = stats.hitCount();
        long missCount = stats.missCount();
        long evictionCount = stats.evictionCount();
        double hitRate = hitCount + missCount == 0 ? 0 : (double) hitCount / (hitCount + missCount);

        log.info(
            "📊 Recommend Auctions Caffeine Cache Stats: hitRate={}%, hit={}, miss={}, evicted={}",
            (int) (hitRate * 100), hitCount, missCount, evictionCount);
    }
}
