package com.sms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpaUpdateEvent {
    private Long userId;
    private Double gpa;
    private int totalCourses;
}
