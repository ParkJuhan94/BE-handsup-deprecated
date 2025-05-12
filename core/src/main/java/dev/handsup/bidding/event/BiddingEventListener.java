package dev.handsup.bidding.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
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
