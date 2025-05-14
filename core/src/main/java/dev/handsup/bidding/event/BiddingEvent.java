package dev.handsup.bidding.event;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;

import lombok.Builder;

import dev.handsup.common.event.DomainEvent;

@Builder(access = PRIVATE)
public record BiddingEvent(
    Long auctionId,
    String sellerEmail,
    Long biddingId,
    Long bidderId,
    String bidderEmail,
    String bidderNickname,
    int biddingPrice,
    BiddingEventType biddingEventType,
    LocalDateTime createdAt
) implements DomainEvent {

    public static BiddingEvent of(
        Long auctionId,
        String sellerEmail,
        Long biddingId,
        Long bidderId,
        String bidderEmail,
        String bidderNickname,
        int biddingPrice,
        LocalDateTime createdAt,
        BiddingEventType biddingEventType
    ) {
        return BiddingEvent.builder()
            .auctionId(auctionId)
            .sellerEmail(sellerEmail)
            .biddingId(biddingId)
            .bidderId(bidderId)
            .bidderEmail(bidderEmail)
            .bidderNickname(bidderNickname)
            .biddingPrice(biddingPrice)
            .createdAt(createdAt)
            .biddingEventType(biddingEventType)
            .build();
    }

    public enum BiddingEventType {
        REGISTERED,  // 입찰 등록
        CANCELED,    // 거래 취소
        COMPLETED    // 거래 완료
    }
}
