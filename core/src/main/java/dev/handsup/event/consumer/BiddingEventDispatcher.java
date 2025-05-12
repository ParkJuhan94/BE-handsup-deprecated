package dev.handsup.event.consumer;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import dev.handsup.bidding.event.BiddingEvent;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "event.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class BiddingEventDispatcher {

    private static final String BIDDING_TOPIC_NAME = "bidding-events";
    private final List<EventHandler<BiddingEvent>> handlers;

    @KafkaListener(
        topics = BIDDING_TOPIC_NAME,
        groupId = "bidding-consumer-group",
        containerFactory = "biddingKafkaListenerContainerFactory"
    )
    public void listen(@Payload BiddingEvent event) {
        boolean handled = false;

        for (EventHandler<BiddingEvent> handler : handlers) {
            if (handler.supports(event)) {
                handler.handle(event);
                handled = true;
                break;
            }
        }

        if (!handled) {
            log.warn("[BIDDING_EVENT] No matching handler found for event: {}", event);
        }

    }
}
