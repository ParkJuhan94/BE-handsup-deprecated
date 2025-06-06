package dev.handsup.event.consumer;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.handsup.bidding.event.BiddingEvent;
import dev.handsup.common.entity.DeadLetterLog;
import dev.handsup.common.entity.DeadLetterLog.DeadLetterStatus;
import dev.handsup.common.repository.DeadLetterLogRepository;
import dev.handsup.common.repository.ProcessedEventLogRepository;
import dev.handsup.common.service.RedisDuplicateChecker;
import dev.handsup.common.service.SlackNotificationService;
import dev.handsup.event.common.EventHandler;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "event.kafka.enabled", havingValue = "true")
public class BiddingEventDispatcher {

    private static final String BIDDING_TOPIC_NAME = "bidding-events";
    private final List<EventHandler<BiddingEvent>> handlers;
    private final RedisDuplicateChecker redisDuplicateChecker;
    private final DeadLetterLogRepository deadLetterLogRepository;
    private final ProcessedEventLogRepository processedEventLogRepository;
    private final ObjectMapper objectMapper;
    private final SlackNotificationService slackNotificationService;

    @RetryableTopic(
        attempts = "4", // 최초 1회 + retry 3회
        backoff = @Backoff(
            delay = 2000, // 1초 후 첫 재시도
            multiplier = 2.0, // 이후 2초 → 4초
            maxDelay = 5000 // 최대 5초까지만 delay
        ),
        dltTopicSuffix = ".dlt",
        include = {
            RuntimeException.class,
            SocketTimeoutException.class,
            IOException.class,
            KafkaException.class,
            RecoverableDataAccessException.class
        },
        timeout = "12000" // 전체 재시도 타임아웃 제한 (12초)
    )
    @KafkaListener(
        topics = BIDDING_TOPIC_NAME,
        groupId = "bidding-consumer-group",
        containerFactory = "biddingKafkaListenerContainerFactory"
    )
    public void listen(
        @Payload BiddingEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topicName
    ) {
        String eventId = event.eventId();
        boolean isRetry = topicName.contains("-retry-");

        if (eventId == null) {
            log.warn("Invalid eventId: null");
            return;
        }

        if (isDuplicate(eventId)) {
            if (isRetry) {
                log.warn("중복 감지 + 재시도 환경 → DLT 유도");
                throw new RuntimeException("🔥 강제 실패: 재시도 중 중복 감지");
            } else {
                log.warn("중복 이벤트 → 무시. eventId={}", eventId);
                return;
            }
        }

        handlers.stream()
            .filter(handler -> handler.supports(event))
            .findFirst()
            .ifPresentOrElse(
                handler -> handler.handle(event),
                () -> log.warn("No matching handler found for BiddingEvent. event={}", event)
            );
    }

    @KafkaListener(
        topics = BIDDING_TOPIC_NAME + ".dlt",
        groupId = "bidding-consumer-group-dlt",
        containerFactory = "biddingKafkaListenerContainerFactory"
    )
    @Transactional
    public DeadLetterLog handleDLT(
        @Payload BiddingEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(name = "x-exception-class", required = false) String exceptionClass,
        @Header(name = "x-exception-message", required = false) String exceptionMessage
    ) throws JsonProcessingException {
        log.warn("Received message from DLT. event={}", event);

        // 1. DB 저장
        String jsonPayload = objectMapper.writeValueAsString(event);

        DeadLetterLog deadLetterLog = DeadLetterLog.builder()
            .originTopic(topic.replace(".dlt", ""))
            .payload(jsonPayload)
            .errorMessage(exceptionMessage != null ? exceptionMessage : "No message")
            .exceptionClass(exceptionClass != null ? exceptionClass : "Unknown")
            .retryCount(0)
            .status(DeadLetterStatus.FAILED)
            .build();

        deadLetterLogRepository.save(deadLetterLog);

        // 2. Slack 알림
        String truncatedPayload = event.toString(); // 필요시 substring 추가
        String slackMessage = String.format(
            "[❗DLT 발생]%n➡️ 예외 클래스: %s%n🧨 에러 메시지: %s%n📦 이벤트 데이터:%n%s",
            exceptionClass,
            exceptionMessage != null ? exceptionMessage : "에러 메시지 없음",
            truncatedPayload.length() > 500 ? truncatedPayload.substring(0, 500) + "..." : truncatedPayload
        );
        slackNotificationService.send(slackMessage);

        // 3. ElasticSearch 로그 저장

        return deadLetterLog;
    }

    private boolean isDuplicate(String eventId) {
        try {
            return redisDuplicateChecker.checkDuplicateAndCacheIfAbsent(eventId);
        } catch (Exception redisEx) {
            log.warn("Redis 장애로 eventId 중복 체크 실패: eventId={}", eventId);
        }

        return processedEventLogRepository.existsByEventId(eventId);
    }
}
