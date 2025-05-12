package dev.handsup.common.support;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig // Embedded Broker가 Test Application Context에 추가됨. -> Broker를 Autowire 해줄 수 있음.
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

    @BeforeAll
    void setupBroker(@Autowired EmbeddedKafkaBroker broker) {
        brokerAddress = broker.getBrokersAsString();
    }
}
