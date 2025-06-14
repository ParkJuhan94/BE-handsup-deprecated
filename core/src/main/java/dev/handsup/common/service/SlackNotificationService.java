package dev.handsup.common.service;

import static dev.handsup.common.exception.ExternalServiceErrorCode.*;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.handsup.common.exception.ExternalServiceException;

@Slf4j
@Service
public class SlackNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String webhookUrl;

    public SlackNotificationService(@Value("${notification.slack.webhook-url}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void send(String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String payload = String.format("{\"text\": \"%s\"}", message);

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            log.error("슬램 알림 전송 실패: {}", e);
            throw new ExternalServiceException(SLACK_SEND_FAILED);
        }
    }
}
