package com.sms.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceApplicationTest {

    @MockBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts successfully
        // with H2 in-memory DB (configured in application-test.yml)
        // and Kafka beans mocked out
    }
}
