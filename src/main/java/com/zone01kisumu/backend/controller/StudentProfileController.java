package com.zone01kisumu.backend.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zone01kisumu.backend.dto.StudentProfileDTOs.ChangePasswordRequest;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.ProfilePictureResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.StudentProfileResponse;
import com.zone01kisumu.backend.dto.StudentProfileDTOs.UpdateStudentProfileRequest;
import com.zone01kisumu.backend.service.StudentProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller providing profile management for authenticated students.
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    /**
     * Retrieves the profile of the currently authenticated student.
     *
     * @param principal Authenticated principal containing the student's email.
     * @return 200 OK with StudentProfileResponse.
     */
    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getProfile(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(studentProfileService.getProfile(email));
    }

    /**
     * Updates profile details and preferences of the currently authenticated student.
     *
     * @param principal Authenticated principal containing the student's email.
     * @param request   Payload with updated profile fields.
     * @return 200 OK with the updated StudentProfileResponse.
     */
    @PutMapping("/profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateStudentProfileRequest request) {
        String email = principal.getName();
        return ResponseEntity.ok(studentProfileService.updateProfile(email, request));
    }

    /**
     * Uploads and updates the profile picture for the specified student.
     *
     * @param principal Authenticated principal containing the student's email.
     * @param studentId ID of the student.
     * @param file      Multipart image file.
     * @return 200 OK with ProfilePictureResponse containing the image URL.
     */
    @PostMapping(value = "/{studentId}/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfilePictureResponse> uploadProfilePicture(
            Principal principal,
            @PathVariable Long studentId,
            @RequestParam("profilePicture") MultipartFile file) {
        String email = principal.getName();
        return ResponseEntity.ok(studentProfileService.uploadProfilePicture(email, studentId, file));
    }

    /**
     * Updates the password of the currently authenticated student.
     *
     * @param principal Authenticated principal containing the student's email.
     * @param request   Payload with current and new passwords.
     * @return 200 OK with confirmation message.
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        String email = principal.getName();
        return ResponseEntity.ok(studentProfileService.changePassword(email, request));
    }
}
