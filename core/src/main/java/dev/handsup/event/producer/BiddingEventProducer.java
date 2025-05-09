package dev.handsup.event.producer;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import dev.handsup.event.common.BiddingEvent;


@Component
@RequiredArgsConstructor
public class BiddingEventProducer {

    private final KafkaTemplate<String, BiddingEvent> kafkaTemplate;

    public void send(BiddingEvent event) {
        kafkaTemplate.send("bidding-events", event);
    }
}
