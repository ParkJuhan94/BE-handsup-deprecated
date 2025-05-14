package dev.handsup.event.consumer;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import dev.handsup.bidding.event.BiddingEvent;
import dev.handsup.event.producer.BiddingEventProducer;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "event.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class BiddingEventListener {

    private final BiddingEventProducer biddingEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void listen(BiddingEvent event) {
        biddingEventProducer.send(event);
    }
}
