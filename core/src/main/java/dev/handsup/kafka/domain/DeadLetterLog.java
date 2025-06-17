package dev.handsup.kafka.domain;

import static dev.handsup.kafka.domain.DeadLetterLog.DeadLetterStatus.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.handsup.common.domain.TimeBaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class DeadLetterLog extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "dead_letter_log_id")
    private Long id;

    @Column(name = "origin_topic")
    private String originTopic;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Column(name = "exception_class")
    private String exceptionClass;

    @Column(name = "exception_message")
    private String exceptionMessage;

    @Enumerated(STRING)
    private DeadLetterStatus status;

    @Column(name = "retry_count")
    private int retryCount;

    @Builder
    public DeadLetterLog(String originTopic, String payload, String exceptionMessage,
        String exceptionClass, int retryCount, DeadLetterStatus status) {
        this.originTopic = originTopic;
        this.payload = payload;
        this.exceptionMessage = exceptionMessage;
        this.exceptionClass = exceptionClass;
        this.retryCount = retryCount;
        this.status = status;
    }

    public static DeadLetterLog fromDuplicateEvent(String topic, Object event, ObjectMapper mapper) {
        return DeadLetterLog.builder()
            .originTopic(topic)
            .payload(toJson(event, mapper))
            .exceptionClass("DuplicateEventSkipException")
            .exceptionMessage("[이벤트 중복]: DLT 유도 하지 않고 무시 처리됨.")
            .status(SKIPPED)
            .build();
    }

    public static DeadLetterLog fromDLTEvent(
        String topic, Object event, String exceptionClass, String exceptionMessage, ObjectMapper mapper
    ) {
        return DeadLetterLog.builder()
            .originTopic(topic.replace(".dlt", ""))
            .payload(toJson(event, mapper))
            .exceptionClass(exceptionClass != null ? exceptionClass : "Unknown")
            .exceptionMessage(exceptionMessage != null ? exceptionMessage : "No message")
            .status(FAILED)
            .retryCount(0)
            .build();
    }

    private static String toJson(Object obj, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    public enum DeadLetterStatus {
        FAILED, SKIPPED, RETRYING, SUCCESS
    }

}
