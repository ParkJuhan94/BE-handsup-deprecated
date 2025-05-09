package dev.handsup.event.common;

import static lombok.AccessLevel.PRIVATE;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder(access = PRIVATE)
public record BiddingEvent(
    Long auctionId,
    String sellerEmail,
    Long biddingId,
    Long bidderId,
    String bidderEmail,
    String bidderNickname,
    int biddingPrice,
    LocalDateTime createdAt,
    EventType eventType
) {

    public static BiddingEvent of(
        Long auctionId,
        String sellerEmail,
        Long biddingId,
        Long bidderId,
        String bidderEmail,
        String bidderNickname,
        int biddingPrice,
        LocalDateTime createdAt
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
            .build();
    }

    /***
     * 컨슈머 쪽에서 switch(event.eventType())으로 분기 처리
     * 로그/알림 시스템에서 특정 이벤트만 필터링 가능
     * 다른 도메인 이벤트랑 섞어서 공통 Kafka Topic으로 보낼 수도 있음
     */
    public enum EventType {
        REGISTERED,  // 입찰 등록
        CANCELED,    // 거래 취소
        COMPLETED    // 거래 완료
    }
}
