package com.sms.user.controller;

import com.sms.user.dto.UpdateProfileRequest;
import com.sms.user.dto.UserProfileDto;
import com.sms.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "User profile management APIs")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile by userId")
    public ResponseEntity<UserProfileDto> getProfileByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user profile by username")
    public ResponseEntity<UserProfileDto> getProfileByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userProfileService.getProfileByUsername(username));
    }

    @GetMapping
    @Operation(summary = "Get all user profiles")
    public ResponseEntity<List<UserProfileDto>> getAllProfiles() {
        return ResponseEntity.ok(userProfileService.getAllProfiles());
    }

    @GetMapping("/students")
    @Operation(summary = "Get all students")
    public ResponseEntity<List<UserProfileDto>> getAllStudents() {
        return ResponseEntity.ok(userProfileService.getAllStudents());
    }

    @GetMapping("/teachers")
    @Operation(summary = "Get all teachers")
    public ResponseEntity<List<UserProfileDto>> getAllTeachers() {
        return ResponseEntity.ok(userProfileService.getAllTeachers());
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserProfileDto> updateProfile(@PathVariable Long userId,
                                                         @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user profile")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) {
        userProfileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
