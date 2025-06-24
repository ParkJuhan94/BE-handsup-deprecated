package dev.handsup.openai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiCompletionService {

    private static final String SYSTEM_PROMPT =
        "너는 중고거래 플랫폼의 상품 설명을 도와주는 AI야.\n"
            + "사용자가 적은 짧은 설명을 보고, 구매자 입장에서 더 상세하고 신뢰 가는 설명으로 바꿔줘.\n"
            + "말투는 존댓말로 정중하게 써줘. 출력문을 그대로 상품 설명에 붙여 넣을거야.\n"
            + "입력: %s";
    private static final String MODEL_NAME = "gpt-3.5-turbo-instruct";
    private final OpenAiService openAiService;

    public String generateDescription(String userInput) {
        try {
            String prompt = String.format(SYSTEM_PROMPT, userInput);

            CompletionRequest request = CompletionRequest.builder()
                .prompt(prompt)
                .model(MODEL_NAME)
                .maxTokens(200)
                .temperature(0.7)
                .build();

            String gptResponse = openAiService.createCompletion(request)
                .getChoices()
                .get(0)
                .getText()
                .trim();

            log.info("✅ GPT 설명 생성 완료: \n입력:\n{}\n{}\n", userInput, gptResponse);

            return gptResponse;
        } catch (Exception e) {
            log.error("OpenAI 생성 실패: 입력={}, 에러={}", userInput, e.getMessage(), e);
            return "죄송합니다. 상품 설명을 생성하지 못했습니다.";
        }
    }

}
