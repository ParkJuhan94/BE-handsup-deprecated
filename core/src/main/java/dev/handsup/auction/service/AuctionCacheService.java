package dev.handsup.auction.service;

import java.time.Duration;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;

import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.auction.exception.AuctionErrorCode;
import dev.handsup.auction.repository.auction.AuctionQueryRepository;
import dev.handsup.common.dto.CommonMapper;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.common.exception.NotFoundException;
import dev.handsup.common.util.KeyGenerator;

@Service
@RequiredArgsConstructor
public class AuctionCacheService {

    private final Cache<String, PageResponse<RecommendAuctionResponse>> caffeineCache;
    private final RedisTemplate<String, PageResponse<RecommendAuctionResponse>> redisTemplate;
    private final AuctionQueryRepository auctionQueryRepository;

    public PageResponse<RecommendAuctionResponse> getRecommendAuctions(
        String si, String gu, String dong, int page, int size, String sort
    ) {
        if (sort == null || sort.isBlank()) {
            throw new NotFoundException(AuctionErrorCode.EMPTY_SORT_INPUT);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.by(sort)));
        String key = KeyGenerator.generateRecommendAuctionsKey(si, gu, dong, pageable);

        // 1. Caffeine 조회
        PageResponse<RecommendAuctionResponse> cached = caffeineCache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }

        // 2. Redis 조회
        cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            caffeineCache.put(key, cached); // Caffeine에도 저장
            return cached;
        }

        // 3. DB 조회
        Slice<RecommendAuctionResponse> auctionSlice = auctionQueryRepository
            .sortAuctionByCriteria(si, gu, dong, pageable);

        PageResponse<RecommendAuctionResponse> response = CommonMapper.toPageResponse(auctionSlice);

        // 4. 캐시에 저장
        caffeineCache.put(key, response);
        redisTemplate.opsForValue().set(key, response, Duration.ofMinutes(10));

        return response;
    }
}
