package dev.handsup.auction.repository.auction;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import dev.handsup.auction.domain.Auction;
import dev.handsup.auction.domain.product.product_category.ProductCategory;
import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.search.dto.AuctionSearchRequest;

public interface AuctionQueryRepository {

    Slice<Auction> searchAuctions(AuctionSearchRequest auctionSearchRequest, Pageable pageable);

    Slice<RecommendAuctionResponse> sortAuctionByCriteria(String si, String gu, String dong,
        Pageable pageable);

    Slice<Auction> findByProductCategories(List<ProductCategory> productCategories,
        Pageable pageable);

    void updateAuctionStatusAfterEndDate();
}
