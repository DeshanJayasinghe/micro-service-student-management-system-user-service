package com.sms.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sms.user.dto.UpdateProfileRequest;
import com.sms.user.dto.UserProfileDto;
import com.sms.user.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserProfileController userProfileController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserProfileDto sampleDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        sampleDto = UserProfileDto.builder()
                .id(1L)
                .userId(100L)
                .username("john.doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("STUDENT")
                .enrolledCourses(3)
                .gpa(3.5)
                .active(true)
                .build();
    }

    @Test
    void getProfileByUserId_whenUserExists_shouldReturn200WithDto() throws Exception {
        when(userProfileService.getProfileByUserId(100L)).thenReturn(sampleDto);

        mockMvc.perform(get("/api/users/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void getProfileByUsername_whenUsernameExists_shouldReturn200WithDto() throws Exception {
        when(userProfileService.getProfileByUsername("john.doe")).thenReturn(sampleDto);

        mockMvc.perform(get("/api/users/username/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"));
    }

    @Test
    void getAllProfiles_shouldReturn200WithList() throws Exception {
        when(userProfileService.getAllProfiles()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllStudents_shouldReturn200WithStudentList() throws Exception {
        when(userProfileService.getAllStudents()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/users/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("STUDENT"));
    }

    @Test
    void getAllTeachers_shouldReturn200WithTeacherList() throws Exception {
        UserProfileDto teacherDto = UserProfileDto.builder()
                .id(2L).userId(200L).username("teacher.jane")
                .email("jane@example.com").role("TEACHER").active(true).build();

        when(userProfileService.getAllTeachers()).thenReturn(List.of(teacherDto));

        mockMvc.perform(get("/api/users/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("TEACHER"));
    }

    @Test
    void updateProfile_withValidRequest_shouldReturn200WithUpdatedDto() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Johnny");
        request.setLastName("Doe");

        UserProfileDto updated = UserProfileDto.builder()
                .id(1L).userId(100L).username("john.doe")
                .email("john@example.com").firstName("Johnny").lastName("Doe")
                .role("STUDENT").active(true).build();

        when(userProfileService.updateProfile(eq(100L), any(UpdateProfileRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/users/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"));
    }

    @Test
    void deleteProfile_whenUserExists_shouldReturn204NoContent() throws Exception {
        doNothing().when(userProfileService).deleteProfile(100L);

        mockMvc.perform(delete("/api/users/100"))
                .andExpect(status().isNoContent());

        verify(userProfileService).deleteProfile(100L);
    }
}
