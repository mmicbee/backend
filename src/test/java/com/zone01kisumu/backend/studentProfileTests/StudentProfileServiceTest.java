package com.zone01kisumu.backend.studentProfileTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.zone01kisumu.backend.dto.StudentProfileDTOs.ChangePasswordRequest;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.NotificationPreferencesDTO;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.ProfilePictureResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.StudentProfileResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.UpdateStudentProfileRequest;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.service.StudentProfileService;
import com.zone01kisumu.backend.storage.CourseFileStorageService;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CourseFileStorageService courseFileStorageService;

    @InjectMocks
    private StudentProfileService studentProfileService;

    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        sampleStudent = Student.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("+254712345678")
                .bio("Aspiring frontend developer")
                .profilePicture("https://example.com/avatar.jpg")
                .password("encoded_pass")
                .notifyCourseUpdates(true)
                .notifyAnnouncements(true)
                .notifyMarketing(false)
                .build();
    }

    @Test
    void getProfile_Success() {
        when(studentRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleStudent));

        StudentProfileResponse response = studentProfileService.getProfile("jane@example.com");

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("Jane Doe", response.getFullName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("+254712345678", response.getPhone());
        assertEquals("Aspiring frontend developer", response.getBio());
        assertEquals("https://example.com/avatar.jpg", response.getProfilePicture());
        assertNotNull(response.getNotificationPreferences());
        assertTrue(response.getNotificationPreferences().getCourseUpdates());
        assertTrue(response.getNotificationPreferences().getAnnouncements());
        assertFalse(response.getNotificationPreferences().getMarketingEmails());
    }

    @Test
    void getProfile_ThrowsWhenNotFound() {
        when(studentRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> studentProfileService.getProfile("unknown@example.com"));
    }

    @Test
    void updateProfile_Success() {
        when(studentRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStudentProfileRequest request = UpdateStudentProfileRequest.builder()
                .firstName("Janet")
                .lastName("Smith")
                .phone("+254799999999")
                .bio("Senior developer")
                .notificationPreferences(NotificationPreferencesDTO.builder()
                        .courseUpdates(false)
                        .announcements(true)
                        .marketingEmails(true)
                        .build())
                .build();

        StudentProfileResponse updated = studentProfileService.updateProfile("jane@example.com", request);

        assertNotNull(updated);
        assertEquals("Janet", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
        assertEquals("Janet Smith", updated.getFullName());
        assertEquals("+254799999999", updated.getPhone());
        assertEquals("Senior developer", updated.getBio());
        assertFalse(updated.getNotificationPreferences().getCourseUpdates());
        assertTrue(updated.getNotificationPreferences().getMarketingEmails());
    }

    @Test
    void updateProfile_ThrowsWhenEmailTaken() {
        Student otherStudent = Student.builder().id(2L).email("other@example.com").build();
        when(studentRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherStudent));

        UpdateStudentProfileRequest request = UpdateStudentProfileRequest.builder()
                .email("other@example.com")
                .build();

        assertThrows(IllegalArgumentException.class, () -> studentProfileService.updateProfile("jane@example.com", request));
    }

    @Test
    void uploadProfilePicture_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(studentRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleStudent));
        when(courseFileStorageService.store(file, "profile-pictures/1")).thenReturn("profile-pictures/1/avatar.png");
        when(courseFileStorageService.resolvePublicUrl("profile-pictures/1/avatar.png"))
                .thenReturn("http://localhost:8080/api/storage/profile-pictures/1/avatar.png");

        ProfilePictureResponse response = studentProfileService.uploadProfilePicture("jane@example.com", 1L, file);

        assertNotNull(response);
        assertEquals("http://localhost:8080/api/storage/profile-pictures/1/avatar.png", response.getProfilePicture());
        assertEquals("http://localhost:8080/api/storage/profile-pictures/1/avatar.png", sampleStudent.getProfilePicture());
        verify(studentRepository).save(sampleStudent);
    }

    @Test
    void uploadProfilePicture_ThrowsWhenIdMismatch() {
        when(studentRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleStudent));
        MultipartFile file = mock(MultipartFile.class);

        assertThrows(AccessDeniedException.class, () -> studentProfileService.uploadProfilePicture("jane@example.com", 999L, file));
    }

    @Test
    void changePassword_Success() {
        when(studentRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleStudent));
        when(passwordEncoder.matches("oldPassword123", "encoded_pass")).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("encoded_new_pass");

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("oldPassword123")
                .newPassword("newPassword456")
                .build();

        Map<String, String> response = studentProfileService.changePassword("jane@example.com", request);

        assertNotNull(response);
        assertEquals("Password updated successfully", response.get("message"));
        assertEquals("encoded_new_pass", sampleStudent.getPassword());
        verify(studentRepository).save(sampleStudent);
    }

    @Test
    void changePassword_ThrowsWhenCurrentPasswordIncorrect() {
        when(studentRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleStudent));
        when(passwordEncoder.matches("wrongPassword", "encoded_pass")).thenReturn(false);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword456")
                .build();

        assertThrows(IllegalArgumentException.class, () -> studentProfileService.changePassword("jane@example.com", request));
    }
}
