package dev.handsup.common.support;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@Slf4j
@SpringJUnitConfig // Embedded Broker가 Test Application Context에 추가됨
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "event.kafka.enabled=true")
public abstract class ApiTestKafkaSupport extends ApiTestSupport {

    private static String brokerAddress;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @DynamicPropertySource
    static void setKafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.producer.bootstrap-servers", () -> brokerAddress);
        registry.add("spring.kafka.consumer.bootstrap-servers", () -> brokerAddress);
    }

    @AfterAll
    static void tearDown(@Autowired EmbeddedKafkaBroker broker) {
        try {
            log.warn("Kafka Embedded Broker is shutting down...");
            broker.destroy();
        } catch (Exception e) {
            log.warn("Kafka 종료 중 예외 발생 (무시): {}", e.getMessage());
        }
    }

    @PostConstruct
    void initKafkaBrokerAddress() {
        brokerAddress = broker.getBrokersAsString(); // ex: "localhost:9092"
    }

}
