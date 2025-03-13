package dev.handsup.producer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import dev.handsup.producer.service.KafkaProducerService;

@Tag(name = "프로듀서 API")
@RequiredArgsConstructor
@RequestMapping("/kafka")
@RestController
public class KafkaProducerController {

	private final KafkaProducerService kafkaProducerService;

	@PostMapping("/send")
	@Operation(summary = "메시지 발행 API", description = "구독자에게 메시지를 발행한다")
	public ResponseEntity<Void> sendMessage(@RequestParam String message) {
		kafkaProducerService.sendMessage(message);
		return ResponseEntity.ok().build();
	}
}
