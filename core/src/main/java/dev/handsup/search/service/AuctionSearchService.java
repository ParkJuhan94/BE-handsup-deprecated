package dev.handsup.search.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.handsup.auction.dto.mapper.AuctionMapper;
import dev.handsup.auction.dto.response.AuctionSimpleResponse;
import dev.handsup.auction.repository.auction.AuctionQueryRepository;
import dev.handsup.common.dto.CommonMapper;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.search.dto.AuctionSearchRequest;
import dev.handsup.search.dto.PopularAuctionKeywordsResponse;
import dev.handsup.search.dto.SearchMapper;
import dev.handsup.search.repository.RedisSearchRepository;

@Service
@RequiredArgsConstructor
public class AuctionSearchService {

    private final AuctionQueryRepository auctionQueryRepository;
    private final RedisSearchRepository redisSearchRepository;

    @Transactional(readOnly = true)
    public PageResponse<AuctionSimpleResponse> searchAuctions(AuctionSearchRequest request, Pageable pageable) {
        Slice<AuctionSimpleResponse> auctionResponsePage = auctionQueryRepository
            .searchAuctions(request, pageable)
            .map(AuctionMapper::toAuctionSimpleResponse);
        redisSearchRepository.increaseSearchCount(request.keyword());

        return CommonMapper.toPageResponse(auctionResponsePage);
    }

    @Transactional(readOnly = true)
    public PopularAuctionKeywordsResponse getPopularAuctionKeywords() {
        return SearchMapper.toPopularAuctionKeywordsResponse(redisSearchRepository.getPopularKeywords(10));
    }
}
