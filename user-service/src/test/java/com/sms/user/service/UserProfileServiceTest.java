package com.sms.user.service;

import com.sms.user.dto.UpdateProfileRequest;
import com.sms.user.dto.UserProfileDto;
import com.sms.user.dto.UserRegisteredEvent;
import com.sms.user.entity.UserProfile;
import com.sms.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserProfile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleProfile = UserProfile.builder()
                .id(1L)
                .userId(100L)
                .username("john.doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserProfile.Role.STUDENT)
                .enrolledCourses(3)
                .gpa(3.5)
                .active(true)
                .build();
    }

    @Test
    void createProfile_whenProfileDoesNotExist_shouldCreateAndReturnDto() {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(100L)
                .username("john.doe")
                .email("john@example.com")
                .role("STUDENT")
                .build();

        when(userProfileRepository.findByUserId(100L)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleProfile);

        UserProfileDto result = userProfileService.createProfile(event);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getUsername()).isEqualTo("john.doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getRole()).isEqualTo("STUDENT");
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void createProfile_whenProfileAlreadyExists_shouldReturnExistingDto() {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(100L)
                .username("john.doe")
                .email("john@example.com")
                .role("STUDENT")
                .build();

        when(userProfileRepository.findByUserId(100L)).thenReturn(Optional.of(sampleProfile));

        UserProfileDto result = userProfileService.createProfile(event);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(100L);
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void getProfileByUserId_whenProfileExists_shouldReturnDto() {
        when(userProfileRepository.findByUserId(100L)).thenReturn(Optional.of(sampleProfile));

        UserProfileDto result = userProfileService.getProfileByUserId(100L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getUsername()).isEqualTo("john.doe");
    }

    @Test
    void getProfileByUserId_whenProfileNotFound_shouldThrowRuntimeException() {
        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getProfileByUserId(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Profile not found for userId: 999");
    }

    @Test
    void getProfileByUsername_whenProfileExists_shouldReturnDto() {
        when(userProfileRepository.findByUsername("john.doe")).thenReturn(Optional.of(sampleProfile));

        UserProfileDto result = userProfileService.getProfileByUsername("john.doe");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john.doe");
    }

    @Test
    void getProfileByUsername_whenUsernameNotFound_shouldThrowRuntimeException() {
        when(userProfileRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getProfileByUsername("ghost"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Profile not found for username: ghost");
    }

    @Test
    void getAllProfiles_shouldReturnAllProfiles() {
        UserProfile teacher = UserProfile.builder()
                .id(2L).userId(200L).username("teacher.jane")
                .email("jane@example.com").role(UserProfile.Role.TEACHER)
                .gpa(0.0).enrolledCourses(0).active(true).build();

        when(userProfileRepository.findAll()).thenReturn(List.of(sampleProfile, teacher));

        List<UserProfileDto> result = userProfileService.getAllProfiles();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllStudents_shouldReturnOnlyStudents() {
        when(userProfileRepository.findByRole(UserProfile.Role.STUDENT))
                .thenReturn(List.of(sampleProfile));

        List<UserProfileDto> result = userProfileService.getAllStudents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo("STUDENT");
    }

    @Test
    void getAllTeachers_shouldReturnOnlyTeachers() {
        UserProfile teacher = UserProfile.builder()
                .id(2L).userId(200L).username("teacher.jane")
                .email("jane@example.com").role(UserProfile.Role.TEACHER)
                .gpa(0.0).enrolledCourses(0).active(true).build();

        when(userProfileRepository.findByRole(UserProfile.Role.TEACHER))
                .thenReturn(List.of(teacher));

        List<UserProfileDto> result = userProfileService.getAllTeachers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo("TEACHER");
    }

    @Test
    void updateProfile_whenProfileExists_shouldUpdateFieldsAndReturnDto() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Johnny");
        request.setLastName("Doe Updated");
        request.setPhoneNumber("0771234567");
        request.setAddress("123 Main St");
        request.setDateOfBirth(LocalDate.of(2000, 1, 15));

        UserProfile updatedProfile = UserProfile.builder()
                .id(1L).userId(100L).username("john.doe")
                .email("john@example.com").firstName("Johnny")
                .lastName("Doe Updated").phoneNumber("0771234567")
                .address("123 Main St").role(UserProfile.Role.STUDENT)
                .gpa(3.5).enrolledCourses(3).active(true).build();

        when(userProfileRepository.findByUserId(100L)).thenReturn(Optional.of(sampleProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(updatedProfile);

        UserProfileDto result = userProfileService.updateProfile(100L, request);

        assertThat(result.getFirstName()).isEqualTo("Johnny");
        assertThat(result.getLastName()).isEqualTo("Doe Updated");
        assertThat(result.getPhoneNumber()).isEqualTo("0771234567");
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_whenProfileNotFound_shouldThrowRuntimeException() {
        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.updateProfile(999L, new UpdateProfileRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Profile not found for userId: 999");
    }

    @Test
    void updateGpa_whenProfileExists_shouldUpdateGpaAndEnrolledCourses() {
        when(userProfileRepository.findByUserId(100L)).thenReturn(Optional.of(sampleProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleProfile);

        userProfileService.updateGpa(100L, 3.8, 5);

        verify(userProfileRepository).save(argThat(p -> p.getGpa() == 3.8 && p.getEnrolledCourses() == 5));
    }

    @Test
    void updateGpa_whenProfileNotFound_shouldThrowRuntimeException() {
        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.updateGpa(999L, 3.5, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Profile not found for userId: 999");
    }

    @Test
    void deleteProfile_whenProfileExists_shouldDeleteProfile() {
        when(userProfileRepository.findByUserId(100L)).thenReturn(Optional.of(sampleProfile));

        userProfileService.deleteProfile(100L);

        verify(userProfileRepository).delete(sampleProfile);
    }

    @Test
    void deleteProfile_whenProfileNotFound_shouldThrowRuntimeException() {
        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.deleteProfile(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Profile not found for userId: 999");
    }

    @Test
    void updateProfile_whenRequestFieldsAreNull_shouldNotOverwriteExistingValues() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        // All fields null — nothing should change

        when(userProfileRepository.findByUserId(100L)).thenReturn(Optional.of(sampleProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleProfile);

        UserProfileDto result = userProfileService.updateProfile(100L, request);

        // Original values preserved
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
    }
}
