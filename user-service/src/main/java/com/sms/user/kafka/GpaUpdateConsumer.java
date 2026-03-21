package com.sms.user.kafka;

import com.sms.user.dto.GpaUpdateEvent;
import com.sms.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GpaUpdateConsumer {

    private final UserProfileService userProfileService;

    @KafkaListener(topics = "marks.updated", groupId = "user-service-group")
    public void consumeGpaUpdate(GpaUpdateEvent event) {
        log.info("Received GPA update event for userId: {}, gpa: {}", event.getUserId(), event.getGpa());
        try {
            userProfileService.updateGpa(event.getUserId(), event.getGpa(), event.getTotalCourses());
            log.info("GPA updated for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing GPA update event for userId: {}: {}", event.getUserId(), e.getMessage());
        }
    }
}
