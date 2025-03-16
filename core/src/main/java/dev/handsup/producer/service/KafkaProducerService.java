package dev.handsup.producer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducerService {

	@Value("${spring.kafka.topic.name}")
	private String topicName;

	private final KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessage(String message) {
		log.info("메시지: [{}]", message);
		CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);

		future.whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("메시지 [{}] 전송 실패", message, ex);
				// TODO 추가적인 에러 처리 로직(예: 재시도, 알림 전송 등)
			} else {
				log.info("메시지 [{}]가 오프셋 {}로 전송되었습니다.", message, result.getRecordMetadata().offset());
			}
		});
	}
}
