package dev.handsup.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import dev.handsup.bidding.event.BiddingEvent;
import dev.handsup.bidding.event.BiddingEvent.BiddingEventType;
import dev.handsup.event.common.EventHandler;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.service.FCMService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterBiddingEventKafkaHandler implements EventHandler<BiddingEvent> {

    private final FCMService fcmService;

    @Override
    public boolean supports(BiddingEvent event) {
        return event.biddingEventType() == BiddingEventType.REGISTERED;
    }

    @Override
    public void handle(@Payload BiddingEvent event) {
        fcmService.sendMessage(
            event.bidderEmail(),
            event.bidderNickname(),
            event.sellerEmail(),
            NotificationType.BIDDING,
            event.auctionId()
        );

        log.info("[REGISTER_BIDDING] : {}", event);
    }
}