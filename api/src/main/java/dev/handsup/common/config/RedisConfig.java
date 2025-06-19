package dev.handsup.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.handsup.auction.dto.response.RecommendAuctionResponse;
import dev.handsup.chat.MessageSubscriber;
import dev.handsup.common.dto.PageResponse;

@Configuration
public class RedisConfig {

    private final String host;
    private final int port;
    private final String password;

    public RedisConfig(
        @Value("${spring.data.redis.host}") String host,
        @Value("${spring.data.redis.port}") int port,
        @Value("${spring.data.redis.password:}") String password
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        if (!password.isEmpty()) {
            config.setPassword(password);
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public PatternTopic chatTopic() {
        return new PatternTopic("/sub/chat-rooms/*");
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
        MessageListenerAdapter messageListener,
        PatternTopic chatTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(messageListener, chatTopic);

        return container;
    }

    @Bean
    public MessageListenerAdapter messageListener(
        ObjectMapper objectMapper,
        RedisTemplate<String, Object> redisTemplate,
        SimpMessageSendingOperations messagingTemplate
    ) {
        return new MessageListenerAdapter(
            new MessageSubscriber(objectMapper, redisTemplate, messagingTemplate));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, PageResponse<RecommendAuctionResponse>> recommendAuctionRedisTemplate(
        RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PageResponse<RecommendAuctionResponse>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

}
