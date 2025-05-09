package dev.handsup.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import dev.handsup.event.common.BiddingEvent;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.service.FCMService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiddingEventConsumer {

    private final FCMService fcmService;

    @KafkaListener(
        topics = "bidding-events",
        groupId = "bidding-consumer-group",
        containerFactory = "biddingKafkaListenerContainerFactory"
    )
    public void listen(@Payload BiddingEvent event) {
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