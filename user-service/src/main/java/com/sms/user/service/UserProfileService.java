package com.sms.user.service;

import com.sms.user.dto.UpdateProfileRequest;
import com.sms.user.dto.UserProfileDto;
import com.sms.user.dto.UserRegisteredEvent;
import com.sms.user.entity.UserProfile;
import com.sms.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfileDto createProfile(UserRegisteredEvent event) {
        log.info("Creating profile for userId: {}", event.getUserId());

        if (userProfileRepository.findByUserId(event.getUserId()).isPresent()) {
            log.warn("Profile already exists for userId: {}", event.getUserId());
            return toDto(userProfileRepository.findByUserId(event.getUserId()).get());
        }

        UserProfile profile = UserProfile.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .role(UserProfile.Role.valueOf(event.getRole()))
                .enrolledCourses(0)
                .gpa(0.0)
                .active(true)
                .build();

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Profile created with id: {}", saved.getId());
        return toDto(saved);
    }

    public UserProfileDto getProfileByUserId(Long userId) {
        log.info("Fetching profile for userId: {}", userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));
        return toDto(profile);
    }

    public UserProfileDto getProfileByUsername(String username) {
        log.info("Fetching profile for username: {}", username);
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Profile not found for username: " + username));
        return toDto(profile);
    }

    public List<UserProfileDto> getAllProfiles() {
        return userProfileRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UserProfileDto> getAllStudents() {
        return userProfileRepository.findByRole(UserProfile.Role.STUDENT).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UserProfileDto> getAllTeachers() {
        return userProfileRepository.findByRole(UserProfile.Role.TEACHER).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for userId: {}", userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Profile updated for userId: {}", userId);
        return toDto(saved);
    }

    @Transactional
    public void updateGpa(Long userId, Double gpa, int totalCourses) {
        log.info("Updating GPA for userId: {}, gpa: {}, totalCourses: {}", userId, gpa, totalCourses);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));
        profile.setGpa(gpa);
        profile.setEnrolledCourses(totalCourses);
        userProfileRepository.save(profile);
        log.info("GPA updated for userId: {}", userId);
    }

    @Transactional
    public void deleteProfile(Long userId) {
        log.info("Deleting profile for userId: {}", userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));
        userProfileRepository.delete(profile);
        log.info("Profile deleted for userId: {}", userId);
    }

    private UserProfileDto toDto(UserProfile profile) {
        return UserProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .dateOfBirth(profile.getDateOfBirth())
                .role(profile.getRole().name())
                .enrolledCourses(profile.getEnrolledCourses())
                .gpa(profile.getGpa())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .active(profile.isActive())
                .build();
    }
}
