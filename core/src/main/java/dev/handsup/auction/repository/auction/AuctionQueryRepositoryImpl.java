package dev.handsup.auction.repository.auction;

import static dev.handsup.auction.domain.QAuction.auction;
import static dev.handsup.auction.domain.product.QProduct.product;
import static dev.handsup.auction.domain.product.product_category.QProductCategory.productCategory;
import static org.springframework.util.StringUtils.hasText;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import dev.handsup.auction.domain.Auction;
import dev.handsup.auction.domain.QAuction;
import dev.handsup.auction.domain.auction_field.AuctionStatus;
import dev.handsup.auction.domain.auction_field.TradeMethod;
import dev.handsup.auction.domain.product.ProductStatus;
import dev.handsup.auction.domain.product.QProduct;
import dev.handsup.auction.domain.product.QProductImage;
import dev.handsup.auction.domain.product.product_category.ProductCategory;
import dev.handsup.auction.dto.request.AuctionSearchCondition;
import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.auction.exception.AuctionErrorCode;
import dev.handsup.common.exception.ValidationException;

@Repository
@RequiredArgsConstructor
public class AuctionQueryRepositoryImpl implements AuctionQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Auction> searchAuctions(AuctionSearchCondition condition, Pageable pageable) {
        List<Auction> content = queryFactory.select(QAuction.auction)
            .from(auction)
            .join(auction.product, product).fetchJoin()
            .leftJoin(product.productCategory, productCategory).fetchJoin()
            .where(
                keywordContains(condition.keyword()),
                categoryEq(condition.productCategory()),
                tradeMethodEq(condition.tradeMethod()),
                siEq(condition.si()),
                guEq(condition.gu()),
                dongEq(condition.dong()),
                initPriceMin(condition.minPrice()),
                initPriceMax(condition.maxPrice()),
                isNewProductEq(condition.isNewProduct()),
                isProgressEq(condition.isProgress())
            )
            .orderBy(searchAuctionSort(pageable))
            .limit(pageable.getPageSize() + 1L)
            .offset(pageable.getOffset())
            .fetch();
        boolean hasNext = hasNextAuction(pageable.getPageSize(), content);
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<RecommendAuctionResponse> sortAuctionByCriteria(String si, String gu, String dong,
        Pageable pageable) {

        QAuction auction = QAuction.auction;
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        List<RecommendAuctionResponse> content = queryFactory
            .select(Projections.constructor(RecommendAuctionResponse.class,
                auction.id,
                auction.title,
                auction.tradingLocation.dong,
                auction.currentBiddingPrice,
                productImage.imageUrl,  // 대표 이미지
                auction.bookmarkCount,
                auction.biddingCount,
                auction.createdAt.stringValue(),
                auction.endDate.stringValue()
            ))
            .from(auction)
            .join(auction.product, product)
            .leftJoin(product.images, productImage).on(productImage.isMain.isTrue())
            .where(
                auction.status.eq(AuctionStatus.BIDDING),
                siEq(si),
                guEq(gu),
                dongEq(dong)
            )
            .orderBy(recommendAuctionSort(pageable))
            .limit(pageable.getPageSize() + 1L)
            .offset(pageable.getOffset())
            .fetch();

        boolean hasNext = hasNextRecommendAuctionResponse(pageable.getPageSize(), content);

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<Auction> findByProductCategories(List<ProductCategory> productCategories,
        Pageable pageable) {
        List<Auction> content = queryFactory.select(QAuction.auction)
            .from(auction)
            .join(auction.product, product).fetchJoin()
            .where(
                auction.product.productCategory.in(productCategories)
            )
            .orderBy(auction.bookmarkCount.desc())
            .limit(pageable.getPageSize() + 1L)
            .offset(pageable.getOffset())
            .fetch();
        boolean hasNext = hasNextAuction(pageable.getPageSize(), content);
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    @Transactional
    public void updateAuctionStatusAfterEndDate() {
        queryFactory
            .update(auction)
            .set(auction.status, AuctionStatus.CANCELED)
            .where(auction.endDate.lt(LocalDate.now()),
                auction.biddingCount.eq(0))
            .execute();

        queryFactory
            .update(auction)
            .set(auction.status, AuctionStatus.TRADING)
            .where(auction.endDate.lt(LocalDate.now()),
                auction.biddingCount.goe(1))
            .execute();
    }

    private OrderSpecifier<?> searchAuctionSort(Pageable pageable) {
        return pageable.getSort().stream()
            .findFirst()
            .map(order -> switch (order.getProperty()) {
                case "BOOKMARK" -> auction.bookmarkCount.desc();
                case "END_DATE" -> auction.endDate.asc();
                case "BIDDING" -> auction.biddingCount.desc();
                default -> auction.createdAt.desc();
            })
            .orElse(auction.createdAt.desc()); // 기본값 최신순
    }

    private OrderSpecifier<?> recommendAuctionSort(Pageable pageable) {
        return pageable.getSort().stream()
            .findFirst()
            .map(order -> switch (order.getProperty()) {
                case "BOOKMARK" -> auction.bookmarkCount.desc();
                case "END_DATE" -> auction.endDate.asc();
                case "BIDDING" -> auction.biddingCount.desc();
                case "CREATED" -> auction.createdAt.desc();
                default ->
                    throw new ValidationException(AuctionErrorCode.INVALID_SORT_INPUT); //기본값 비허용
            })
            .orElseThrow();
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null ? auction.title.contains(keyword) : null;
    }

    private BooleanExpression categoryEq(String productCategory) {
        return hasText(productCategory) ? product.productCategory.value.eq(productCategory) : null;
    }

    private BooleanExpression tradeMethodEq(String tradeMethod) {
        return hasText(tradeMethod) ? auction.tradeMethod.eq(TradeMethod.of(tradeMethod)) : null;
    }

    private BooleanExpression siEq(String si) {
        return hasText(si) ? auction.tradingLocation.si.eq(si) : null;
    }

    private BooleanExpression guEq(String gu) {
        return hasText(gu) ? auction.tradingLocation.gu.eq(gu) : null;
    }

    private BooleanExpression dongEq(String dong) {
        return hasText(dong) ? auction.tradingLocation.dong.eq(dong) : null;
    }

    private BooleanExpression initPriceMin(Integer minPrice) {
        return (minPrice != null) ? auction.initPrice.goe(minPrice) : null;
    }

    private BooleanExpression initPriceMax(Integer maxPrice) {
        return (maxPrice != null) ? auction.initPrice.loe(maxPrice) : null;
    }

    private BooleanExpression isNewProductEq(Boolean isNewProduct) {
        if (isNewProduct == null) {
            return null;
        }
        if (Boolean.TRUE.equals(isNewProduct)) {
            return auction.product.status.eq(ProductStatus.NEW);
        } else {
            return auction.product.status.eq(ProductStatus.CLEAN)
                .or(auction.product.status.eq(ProductStatus.DIRTY));
        }
    }

    private BooleanExpression isProgressEq(Boolean isProgress) {
        if (Boolean.TRUE.equals(isProgress)) {
            return auction.status.eq(AuctionStatus.BIDDING);
        }
        return null;
    }

    private boolean hasNextAuction(int pageSize, List<Auction> auctions) {
        if (auctions.size() <= pageSize) {
            return false;
        }
        auctions.remove(pageSize);
        return true;
    }

    private boolean hasNextRecommendAuctionResponse(int pageSize,
        List<RecommendAuctionResponse> recommendAuctionResponses) {
        if (recommendAuctionResponses.size() <= pageSize) {
            return false;
        }
        recommendAuctionResponses.remove(pageSize);
        return true;
    }
}
