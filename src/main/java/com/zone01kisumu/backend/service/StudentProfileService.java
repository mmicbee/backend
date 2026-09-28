package com.zone01kisumu.backend.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zone01kisumu.backend.dto.StudentProfileDTOs.ChangePasswordRequest;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.NotificationPreferencesDTO;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.ProfilePictureResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.StudentProfileResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.UpdateStudentProfileRequest;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.repository.StudentRepository;
import com.zone01kisumu.backend.storage.CourseFileStorageService;
import com.zone01kisumu.backend.util.LoggerUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service managing student profile retrieval, updates, picture uploads, and password changes.
 */
@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseFileStorageService courseFileStorageService;

    /**
     * Retrieves the profile of the authenticated student by their email.
     *
     * @param email Email from the JWT authenticated context.
     * @return StudentProfileResponse containing personal details and preferences.
     */
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(String email) {
        Student student = getStudentByEmail(email);
        return mapToProfileResponse(student);
    }

    /**
     * Updates profile details and notification preferences of the authenticated student.
     *
     * @param email   Authenticated student's email.
     * @param request Payload containing updated details.
     * @return StudentProfileResponse with updated data.
     */
    @Transactional
    public StudentProfileResponse updateProfile(String email, UpdateStudentProfileRequest request) {
        Student student = getStudentByEmail(email);

        if (request.getEmail() != null && !request.getEmail().trim().equalsIgnoreCase(student.getEmail())) {
            String newEmail = request.getEmail().trim();
            Optional<Student> existing = studentRepository.findByEmail(newEmail);
            if (existing.isPresent() && !existing.get().getId().equals(student.getId())) {
                throw new IllegalArgumentException("Email already in use by another account");
            }
            student.setEmail(newEmail);
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            student.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            student.setLastName(request.getLastName().trim());
        }

        if ((request.getFirstName() == null || request.getFirstName().isBlank())
                && (request.getLastName() == null || request.getLastName().isBlank())
                && request.getFullName() != null && !request.getFullName().isBlank()) {
            String[] parts = request.getFullName().trim().split("\\s+", 2);
            student.setFirstName(parts[0]);
            if (parts.length > 1) {
                student.setLastName(parts[1]);
            }
        }

        if (request.getPhone() != null) {
            student.setPhone(request.getPhone().trim());
        }

        if (request.getBio() != null) {
            student.setBio(request.getBio().trim());
        }

        if (request.getProfilePicture() != null && !request.getProfilePicture().isBlank()) {
            student.setProfilePicture(request.getProfilePicture().trim());
        }

        if (request.getNotificationPreferences() != null) {
            NotificationPreferencesDTO prefs = request.getNotificationPreferences();
            if (prefs.getCourseUpdates() != null) {
                student.setNotifyCourseUpdates(prefs.getCourseUpdates());
            }
            if (prefs.getAnnouncements() != null) {
                student.setNotifyAnnouncements(prefs.getAnnouncements());
            }
            if (prefs.getMarketingEmails() != null) {
                student.setNotifyMarketing(prefs.getMarketingEmails());
            }
        }

        Student updated = studentRepository.save(student);
        LoggerUtil.logInfo("Updated student profile for studentId={}", updated.getId());

        return mapToProfileResponse(updated);
    }

    /**
     * Uploads and updates a student's profile picture.
     *
     * @param email     Authenticated student's email.
     * @param studentId ID of the student target.
     * @param file      Uploaded image file.
     * @return ProfilePictureResponse with the public image URL.
     */
    @Transactional
    public ProfilePictureResponse uploadProfilePicture(String email, Long studentId, MultipartFile file) {
        Student student = getStudentByEmail(email);

        if (!student.getId().equals(studentId)) {
            throw new AccessDeniedException("Cannot update another student's profile picture");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile picture file cannot be empty");
        }

        try {
            String storageKey = courseFileStorageService.store(file, "profile-pictures/" + studentId);
            String publicUrl = courseFileStorageService.resolvePublicUrl(storageKey);
            student.setProfilePicture(publicUrl);
            studentRepository.save(student);

            LoggerUtil.logInfo("Uploaded profile picture for studentId={}, URL={}", studentId, publicUrl);
            return ProfilePictureResponse.builder()
                    .profilePicture(publicUrl)
                    .message("Profile picture updated successfully")
                    .build();
        } catch (IOException e) {
            LoggerUtil.logRuntimeError("Profile Picture Upload", "Failed to save profile picture", e);
            throw new RuntimeException("Failed to upload profile picture: " + e.getMessage(), e);
        }
    }

    /**
     * Changes the password of the authenticated student.
     *
     * @param email   Authenticated student's email.
     * @param request Payload containing current and new passwords.
     * @return Confirmation message map.
     */
    @Transactional
    public Map<String, String> changePassword(String email, ChangePasswordRequest request) {
        Student student = getStudentByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), student.getPassword())) {
            throw new IllegalArgumentException("Current password incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        student.setPassword(passwordEncoder.encode(request.getNewPassword()));
        studentRepository.save(student);

        LoggerUtil.logInfo("Password successfully changed for studentId={}", student.getId());
        return Map.of("message", "Password updated successfully");
    }

    private Student getStudentByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authenticated student email is required");
        }
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for email: " + email));
    }

    private StudentProfileResponse mapToProfileResponse(Student student) {
        String fullName = ((student.getFirstName() != null ? student.getFirstName() : "")
                + " " + (student.getLastName() != null ? student.getLastName() : "")).trim();

        NotificationPreferencesDTO preferences = NotificationPreferencesDTO.builder()
                .courseUpdates(student.getNotifyCourseUpdates() != null ? student.getNotifyCourseUpdates() : true)
                .announcements(student.getNotifyAnnouncements() != null ? student.getNotifyAnnouncements() : true)
                .marketingEmails(student.getNotifyMarketing() != null ? student.getNotifyMarketing() : false)
                .build();

        return StudentProfileResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(fullName)
                .email(student.getEmail())
                .phone(student.getPhone())
                .bio(student.getBio())
                .profilePicture(student.getProfilePicture())
                .notificationPreferences(preferences)
                .build();
    }
}
