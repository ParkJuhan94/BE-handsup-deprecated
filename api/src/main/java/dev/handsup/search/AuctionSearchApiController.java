package dev.handsup.search.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import dev.handsup.auction.dto.response.AuctionSimpleResponse;
import dev.handsup.auth.annotation.NoAuth;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.search.dto.AuctionSearchRequest;
import dev.handsup.search.dto.PopularAuctionKeywordsResponse;
import dev.handsup.search.service.AuctionSearchService;

@Tag(name = "경매 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions/search")
public class AuctionSearchApiController {

    private final AuctionSearchService auctionSearchService;

    @NoAuth
    @Operation(summary = "경매 검색 API", description = "경매를 검색한다")
    @ApiResponse(useReturnTypeSchema = true)
    @PostMapping
    public ResponseEntity<PageResponse<AuctionSimpleResponse>> searchAuctions(
        @Valid @RequestBody AuctionSearchRequest request,
        Pageable pageable) {
        PageResponse<AuctionSimpleResponse> response = auctionSearchService.searchAuctions(request, pageable);
        return ResponseEntity.ok(response);
    }

    @NoAuth
    @Operation(summary = "인기 경매 검색어 조회 API", description = "인기 경매 검색어를 조회한다.")
    @ApiResponse(useReturnTypeSchema = true)
    @GetMapping("/popular")
    public ResponseEntity<PopularAuctionKeywordsResponse> getPopularAuctionKeywords() {
        PopularAuctionKeywordsResponse response = auctionSearchService.getPopularAuctionKeywords();
        return ResponseEntity.ok(response);
    }
}
