package com.sms.user.kafka;

import com.sms.user.dto.UserRegisteredEvent;
import com.sms.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final UserProfileService userProfileService;

    @KafkaListener(topics = "user.registered", groupId = "user-service-group")
    public void consumeUserRegistered(UserRegisteredEvent event) {
        log.info("Received user registered event for userId: {}", event.getUserId());
        try {
            userProfileService.createProfile(event);
            log.info("User profile created for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user registered event for userId: {}: {}", event.getUserId(), e.getMessage());
        }
    }
}
