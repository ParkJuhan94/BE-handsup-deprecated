package dev.handsup.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.benmanes.caffeine.cache.Cache;

import dev.handsup.auction.domain.Auction;
import dev.handsup.auction.dto.mapper.AuctionMapper;
import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.auction.exception.AuctionErrorCode;
import dev.handsup.auction.repository.auction.AuctionQueryRepository;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.common.exception.NotFoundException;
import dev.handsup.common.util.CacheKeyGenerator;
import dev.handsup.fixture.AuctionFixture;
import dev.handsup.fixture.UserFixture;
import dev.handsup.user.domain.User;

@DisplayName("[경매 캐시 서비스 테스트]")
@ExtendWith(MockitoExtension.class)
class AuctionCacheServiceTest {

    private static final User user = UserFixture.user1();
    private static final Auction auction1 = AuctionFixture.auction(1L, user);
    private static final Auction auction2 = AuctionFixture.auction(2L, user);

    private static Pageable pageable;
    private static String key;
    private static PageResponse<RecommendAuctionResponse> cachedResponse;
    @Mock
    private RedisTemplate<String, PageResponse<RecommendAuctionResponse>> redisTemplate;
    @Mock
    private ValueOperations<String, PageResponse<RecommendAuctionResponse>> valueOperations;
    @Mock
    private Cache<String, PageResponse<RecommendAuctionResponse>> caffeineCache;
    @Mock
    private AuctionQueryRepository auctionQueryRepository;
    @InjectMocks
    private AuctionCacheService auctionCacheService;

    @BeforeAll
    static void beforeAll() {
        ReflectionTestUtils.setField(auction1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(auction2, "createdAt", LocalDateTime.now());

        auction1.increaseBookmarkCount();
        auction2.increaseBookmarkCount();
        auction2.increaseBookmarkCount();

        pageable = PageRequest.of(
            0, 5, Sort.by("BOOKMARK")
        );

        key = CacheKeyGenerator.recommendAuctionsKey(
            auction1.getTradingLocation().getSi(),
            auction1.getTradingLocation().getGu(),
            auction1.getTradingLocation().getDong(),
            pageable
        );

        cachedResponse = new PageResponse<>(
            List.of(
                AuctionMapper.toRecommendAuctionResponse(auction1),
                AuctionMapper.toRecommendAuctionResponse(auction2)
            ),
            2,
            true
        );
    }

    @Test
    @DisplayName("[Caffeine 캐시에 값이 있으면 바로 반환한다.]")
    void getRecommendAuctions_caffeineHit() {
        // given
        given(caffeineCache.getIfPresent(key)).willReturn(cachedResponse);
        System.out.println(key);
        // when
        PageResponse<RecommendAuctionResponse> response = auctionCacheService.getRecommendAuctions(
            auction1.getTradingLocation().getSi(),
            auction1.getTradingLocation().getGu(),
            auction1.getTradingLocation().getDong(),
            0, 5, "BOOKMARK"
        );

        // then
        assertThat(response.content())
            .hasSize(2)
            .extracting(RecommendAuctionResponse::auctionId)
            .containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("[Redis에 값이 있으면 Caffeine에도 저장하고 반환한다.]")
    void getRecommendAuctions_redisHit() {
        // given
        given(caffeineCache.getIfPresent(key)).willReturn(null);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(redisTemplate.opsForValue().get(key)).willReturn(cachedResponse);

        // when
        PageResponse<RecommendAuctionResponse> response = auctionCacheService.getRecommendAuctions(
            auction1.getTradingLocation().getSi(),
            auction1.getTradingLocation().getGu(),
            auction1.getTradingLocation().getDong(),
            0, 5, "BOOKMARK"
        );

        // then
        assertThat(response.content())
            .hasSize(2)
            .extracting(RecommendAuctionResponse::auctionId)
            .containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("[캐시에 아무것도 없으면 DB 조회 후 캐시에 저장한다.]")
    void getRecommendAuctions_dbHit() {
        // given
        given(caffeineCache.getIfPresent(key)).willReturn(null);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(redisTemplate.opsForValue().get(key)).willReturn(null);
        given(auctionQueryRepository.sortAuctionByCriteria(any(), any(), any(), any()))
            .willReturn(
                new SliceImpl<>(List.of(auction1, auction2), pageable, false));

        // when
        PageResponse<RecommendAuctionResponse> response = auctionCacheService.getRecommendAuctions(
            auction1.getTradingLocation().getSi(),
            auction1.getTradingLocation().getGu(),
            auction1.getTradingLocation().getDong(),
            0, 5, "BOOKMARK"
        );

        // then
        assertThat(response.content()).isNotEmpty();
        assertThat(response.content())
            .hasSize(2)
            .extracting(RecommendAuctionResponse::auctionId)
            .containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("[정렬 조건이 없으면 예외를 던진다.]")
    void getRecommendAuctions_noSort_throwsException() {
        // when, then
        assertThatThrownBy(() -> auctionCacheService.getRecommendAuctions(
            auction1.getTradingLocation().getSi(),
            auction1.getTradingLocation().getGu(),
            auction1.getTradingLocation().getDong(),
            0, 5, null
        ))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(AuctionErrorCode.EMPTY_SORT_INPUT.getMessage());
    }
}
