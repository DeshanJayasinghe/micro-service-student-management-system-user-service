package com.sms.auth.kafka;

import com.sms.auth.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private static final String TOPIC = "user.registered";
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publishing user registered event: {}", event);
        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);
    }
}
