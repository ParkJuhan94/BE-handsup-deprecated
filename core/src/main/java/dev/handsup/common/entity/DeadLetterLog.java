package dev.handsup.common.entity;

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

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "exception_class")
    private String exceptionClass;

    @Column(name = "retry_count")
    private int retryCount;

    @Enumerated(STRING)
    private DeadLetterStatus status;

    @Builder
    public DeadLetterLog(String originTopic, String payload, String errorMessage,
        String exceptionClass, int retryCount, DeadLetterStatus status) {
        this.originTopic = originTopic;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.exceptionClass = exceptionClass;
        this.retryCount = retryCount;
        this.status = status;
    }

    public enum DeadLetterStatus {
        FAILED, RETRYING, SUCCESS
    }
}
