package dev.handsup.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.theokanning.openai.service.OpenAiService;

@Configuration
public class OpenAiConfig {

    private final String apiKey;

    public OpenAiConfig(@Value("${openai.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey);
    }
}
