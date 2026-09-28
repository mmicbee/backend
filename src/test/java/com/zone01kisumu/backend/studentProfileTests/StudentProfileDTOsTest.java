package com.zone01kisumu.backend.studentProfileTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.zone01kisumu.backend.dto.StudentProfileDTOs.ChangePasswordRequest;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.NotificationPreferencesDTO;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.ProfilePictureResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.StudentProfileResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.UpdateStudentProfileRequest;

class StudentProfileDTOsTest {

    @Test
    void testNotificationPreferencesDTO() {
        NotificationPreferencesDTO prefs = NotificationPreferencesDTO.builder()
                .courseUpdates(true)
                .announcements(false)
                .marketingEmails(true)
                .build();

        assertTrue(prefs.getCourseUpdates());
        assertFalse(prefs.getAnnouncements());
        assertTrue(prefs.getMarketingEmails());
    }

    @Test
    void testStudentProfileResponseDTO() {
        NotificationPreferencesDTO prefs = new NotificationPreferencesDTO();
        StudentProfileResponse response = StudentProfileResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+254700000000")
                .bio("Bio")
                .profilePicture("pic.png")
                .notificationPreferences(prefs)
                .build();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("John Doe", response.getFullName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("+254700000000", response.getPhone());
    }

    @Test
    void testUpdateStudentProfileRequestDTO() {
        UpdateStudentProfileRequest request = UpdateStudentProfileRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("+254711111111")
                .bio("Updated Bio")
                .build();

        assertEquals("Jane Doe", request.getFullName());
        assertEquals("jane@example.com", request.getEmail());
        assertEquals("+254711111111", request.getPhone());
        assertEquals("Updated Bio", request.getBio());
    }

    @Test
    void testChangePasswordRequestDTO() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("currentPass123")
                .newPassword("newPass456")
                .build();

        assertEquals("currentPass123", request.getCurrentPassword());
        assertEquals("newPass456", request.getNewPassword());
    }

    @Test
    void testProfilePictureResponseDTO() {
        ProfilePictureResponse response = ProfilePictureResponse.builder()
                .profilePicture("http://localhost/pic.jpg")
                .message("Success")
                .build();

        assertEquals("http://localhost/pic.jpg", response.getProfilePicture());
        assertEquals("Success", response.getMessage());
    }
}
