package dev.handsup.kafka.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import dev.handsup.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum KafkaErrorCode implements ErrorCode {

    DUPLICATE_RETRY_EVENT(
        "재시도 환경에서 중복 이벤트가 감지되어 DLT로 유도합니다.",
        "KAFKA_001"
    ),

    UNSUPPORTED_EVENT_TYPE(
        "해당 이벤트 타입을 처리할 수 없습니다.",
        "KAFKA_002"
    );

    private final String message;
    private final String code;
}
