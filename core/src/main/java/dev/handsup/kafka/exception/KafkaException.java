package dev.handsup.kafka.exception;

import lombok.Getter;

import dev.handsup.common.exception.ErrorCode;

@Getter
public class KafkaException extends RuntimeException {

    private final String code;

    public KafkaException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
