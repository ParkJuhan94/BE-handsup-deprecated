package dev.handsup.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import dev.handsup.event.common.BiddingEvent;
import dev.handsup.event.producer.BiddingEventProducer;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiddingEventHandler {

    private final BiddingEventProducer biddingEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegisterBidding(BiddingEvent event) {
        biddingEventProducer.send(event);
    }
}
