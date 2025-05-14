package dev.handsup.event.producer;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import dev.handsup.bidding.event.BiddingEvent;

@Component
@RequiredArgsConstructor
public class BiddingEventProducer {

    private static final String BIDDING_TOPIC_NAME = "bidding-events";
    private final KafkaTemplate<String, BiddingEvent> kafkaTemplate;

    public void send(BiddingEvent event) {
        kafkaTemplate.send(BIDDING_TOPIC_NAME, event);
    }
}
