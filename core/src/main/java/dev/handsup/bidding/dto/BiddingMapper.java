package dev.handsup.bidding.dto;

import static lombok.AccessLevel.*;

import java.util.UUID;

import lombok.NoArgsConstructor;

import dev.handsup.auction.domain.Auction;
import dev.handsup.bidding.domain.Bidding;
import dev.handsup.bidding.dto.response.BiddingResponse;
import dev.handsup.bidding.event.BiddingEvent;
import dev.handsup.bidding.event.BiddingEvent.BiddingEventType;
import dev.handsup.user.domain.User;

@NoArgsConstructor(access = PRIVATE)
public class BiddingMapper {

    public static Bidding toBidding(int biddingPrice, Auction auction, User bidder) {
        return Bidding.of(
            biddingPrice,
            auction,
            bidder
        );
    }

    public static BiddingResponse toBiddingResponse(Bidding bidding) {
        return BiddingResponse.of(
            bidding.getId(),
            bidding.getBiddingPrice(),
            bidding.getAuction().getId(),
            bidding.getBidder().getId(),
            bidding.getBidder().getNickname(),
            bidding.getTradingStatus().getLabel(),
            bidding.getBidder().getProfileImageUrl(),
            bidding.getCreatedAt().toString()
        );
    }

    public static BiddingEvent toBiddingEvent(Bidding bidding, BiddingEventType biddingEventType) {
        Auction auction = bidding.getAuction();
        User bidder = bidding.getBidder();
        String eventId = UUID.randomUUID().toString();

        return BiddingEvent.of(
            eventId,
            auction.getId(),
            auction.getSeller().getEmail(),
            bidding.getId(),
            bidder.getId(),
            bidder.getEmail(),
            bidder.getNickname(),
            bidding.getBiddingPrice(),
            bidding.getCreatedAt(),
            biddingEventType
        );
    }
}
