package dev.handsup.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExternalServiceErrorCode implements ErrorCode {

    FIREBASE_SEND_FAILED("FCM 메시지 전송에 실패했습니다.", "EXTERNAL_001"),
    SLACK_SEND_FAILED("슬랙 메시지 전송에 실패했습니다.", "EXTERNAL_002");
//    PAYMENT_API_ERROR("결제 API 호출 중 오류가 발생했습니다.", "EXTERNAL_003"),
//    SMS_GATEWAY_ERROR("SMS 발송에 실패했습니다.", "EXTERNAL_004");

    private final String message;
    private final String code;
}
