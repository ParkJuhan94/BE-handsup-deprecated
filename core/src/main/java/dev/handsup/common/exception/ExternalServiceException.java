package dev.handsup.common.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {

    private final String code;

    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
