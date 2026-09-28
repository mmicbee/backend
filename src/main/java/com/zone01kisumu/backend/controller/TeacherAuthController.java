package com.zone01kisumu.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.dto.AuthResponse;
import com.zone01kisumu.backend.dto.TeacherLogin;
import com.zone01kisumu.backend.dto.TeacherRegistration;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.service.TeacherAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for handling teacher authentication (register and login).
 */

@RestController
@RequestMapping("/api/auth/teacher")
@RequiredArgsConstructor
public class TeacherAuthController {

    private final TeacherAuthService teacherAuthService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerTeacher(@Valid @RequestBody TeacherRegistration request) {
        final Teacher savedTeacher = teacherAuthService.registerTeacher(request);

        final AuthResponse response = AuthResponse.builder()
                .message("Teacher registration successful!")
                .userId(savedTeacher.getId().toString())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginTeacher(@Valid @RequestBody TeacherLogin request) {
        final Teacher teacher = teacherAuthService.loginTeacher(request);

        final String jwtToken = jwtUtil.generateToken(teacher.getEmail(), "TEACHER");

        final AuthResponse response = AuthResponse.builder()
                .message("Teacher login successful!")
                .userId(teacher.getId().toString())
                .token(jwtToken)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateTeacherProfile(
            @PathVariable Long id, @Valid @RequestBody TeacherRegistration request) {
        teacherAuthService.updateTeacherProfile(id, request);
        return new ResponseEntity<>("Teacher profile updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTeacherProfile(@PathVariable Long id) {
        teacherAuthService.deleteTeacherProfile(id);
        return new ResponseEntity<>("Teacher profile deleted successfully", HttpStatus.OK);
    }

    //get teacher by id
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Teacher teacher = teacherAuthService.getTeacherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + id));
        return ResponseEntity.ok(teacher);
    }
}
