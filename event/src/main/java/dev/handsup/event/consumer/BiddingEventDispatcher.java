package dev.handsup.event.consumer;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import dev.handsup.bidding.event.BiddingEvent;
import dev.handsup.event.common.EventHandler;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "event.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class BiddingEventDispatcher {

    private static final String BIDDING_TOPIC_NAME = "bidding-events";
    private static final String BIDDING_TOPIC_DLT_NAME = BIDDING_TOPIC_NAME + ".dlt";
    private final List<EventHandler<BiddingEvent>> handlers;

    @RetryableTopic(
        attempts = "4", // 최초 1회 + retry 3회
        backoff = @Backoff(
            delay = 1000, // 1초 후 첫 재시도
            multiplier = 2.0, // 이후 2초 → 4초
            maxDelay = 5000 // 최대 5초까지만 delay
        ),
        dltTopicSuffix = ".dlt", // 실패 시 전송될 DLT 토픽 접미사
        autoCreateTopics = "true",
        include = {RuntimeException.class}, // 대상 예외 지정 (생략 시 모든 Exception),
        timeout = "60000" // 전체 재시도 타임아웃 제한 (1분)
    )
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

    @KafkaListener(
        topics = BIDDING_TOPIC_DLT_NAME,
        groupId = "bidding-consumer-group-dlt",
        containerFactory = "biddingKafkaListenerContainerFactory"
    )
    public void handleDLT(@Payload BiddingEvent event) {
        log.error("❌ DLT 메시지 도착: {}", event);
        // TODO: Elastic에 기록 or DB 저장 등 처리
    }
}
