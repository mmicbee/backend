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
import com.zone01kisumu.backend.dto.StudentLogin;
import com.zone01kisumu.backend.dto.StudentRegistration;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.service.StudentAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for handling student authentication (register and login).
 */

@RestController
@RequestMapping("/api/auth/student")
@RequiredArgsConstructor
public class StudentAuthController {

    private final StudentAuthService studentAuthService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerStudent(@Valid @RequestBody StudentRegistration request) {
        final Student savedstudent = studentAuthService.registerStudent(request);

        final AuthResponse response = AuthResponse.builder()
                .message("student registration successful!")
                .userId(savedstudent.getId().toString())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginStudent(@Valid @RequestBody StudentLogin request) {
        final Student student = studentAuthService.loginStudent(request);

        final String jwtToken = jwtUtil.generateToken(student.getEmail(), "STUDENT");
        final AuthResponse response = AuthResponse.builder()
                .message("student login successful!")
                .userId(student.getId().toString())
                .token(jwtToken)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteStudentProfile(@PathVariable Long id) {
        studentAuthService.deleteStudentProfile(id);
        return ResponseEntity.ok("Student profile deleted successfully");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateStudentProfile(
            @PathVariable Long id, @RequestBody StudentRegistration request) {
        studentAuthService.updateStudentProfile(id, request);
        return ResponseEntity.ok("Student profile updated successfully");
    }

    //get student by id
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentAuthService.getStudentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));
        return ResponseEntity.ok(student); 
    }
}
