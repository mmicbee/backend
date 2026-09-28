package com.zone01kisumu.backend.studentProfileTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.zone01kisumu.backend.controller.StudentProfileController;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.ChangePasswordRequest;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.NotificationPreferencesDTO;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.ProfilePictureResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.StudentProfileResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.UpdateStudentProfileRequest;
import com.zone01kisumu.backend.service.StudentProfileService;

@ExtendWith(MockitoExtension.class)
class StudentProfileControllerTest {

    @Mock
    private StudentProfileService studentProfileService;

    @InjectMocks
    private StudentProfileController studentProfileController;

    private Principal principal;
    private StudentProfileResponse sampleProfile;

    @BeforeEach
    void setUp() {
        principal = () -> "jane@example.com";
        sampleProfile = StudentProfileResponse.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("+254712345678")
                .bio("Aspiring frontend developer")
                .profilePicture("https://example.com/avatar.jpg")
                .notificationPreferences(NotificationPreferencesDTO.builder()
                        .courseUpdates(true)
                        .announcements(true)
                        .marketingEmails(false)
                        .build())
                .build();
    }

    @Test
    void getProfile_returnsOk() {
        when(studentProfileService.getProfile("jane@example.com")).thenReturn(sampleProfile);

        ResponseEntity<StudentProfileResponse> response = studentProfileController.getProfile(principal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Jane", response.getBody().getFirstName());
        assertEquals("jane@example.com", response.getBody().getEmail());
        verify(studentProfileService).getProfile("jane@example.com");
    }

    @Test
    void updateProfile_returnsOk() {
        UpdateStudentProfileRequest request = UpdateStudentProfileRequest.builder()
                .bio("Updated bio")
                .build();

        when(studentProfileService.updateProfile(eq("jane@example.com"), any(UpdateStudentProfileRequest.class)))
                .thenReturn(sampleProfile);

        ResponseEntity<StudentProfileResponse> response = studentProfileController.updateProfile(principal, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(studentProfileService).updateProfile(eq("jane@example.com"), eq(request));
    }

    @Test
    void uploadProfilePicture_returnsOk() {
        MultipartFile file = mock(MultipartFile.class);
        ProfilePictureResponse pictureResponse = ProfilePictureResponse.builder()
                .profilePicture("http://localhost:8080/api/storage/avatar.jpg")
                .message("Profile picture updated successfully")
                .build();

        when(studentProfileService.uploadProfilePicture("jane@example.com", 1L, file))
                .thenReturn(pictureResponse);

        ResponseEntity<ProfilePictureResponse> response = studentProfileController.uploadProfilePicture(
                principal, 1L, file);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://localhost:8080/api/storage/avatar.jpg", response.getBody().getProfilePicture());
    }

    @Test
    void changePassword_returnsOk() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("oldPass")
                .newPassword("newPass123")
                .build();

        when(studentProfileService.changePassword("jane@example.com", request))
                .thenReturn(Map.of("message", "Password updated successfully"));

        ResponseEntity<Map<String, String>> response = studentProfileController.changePassword(principal, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password updated successfully", response.getBody().get("message"));
    }
}
