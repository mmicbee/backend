package com.zone01kisumu.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.dto.InstitutionLoginDTO;
import com.zone01kisumu.backend.dto.InstitutionRegistrationDTO;
import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.service.InstitutionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

//Institution  Controller
@RestController
@RequestMapping("/api/auth/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final AuthenticationManager authenticationManager;
    private final InstitutionService institutionService;
    private final JwtUtil jwtUtil;

    // Register a new institution
    @PostMapping("/register")
    public ResponseEntity<Object> registerInstitution(@Valid @RequestBody InstitutionRegistrationDTO registrationDTO) {
        try {
            // Save institution and convert to DTO
            Institution savedInstitution = institutionService.register(registrationDTO);
            InstitutionRegistrationDTO institutionDTO = InstitutionService.mapToRegistrationDTO(savedInstitution);

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Institution registered successfully",
                    "institution", institutionDTO));
        } catch (Exception e) {
            // Return error response
            Map<String, String> error = new HashMap<>();
            error.put("error", "Institution registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Institution login
    @PostMapping("/login")
    public ResponseEntity<Object> loginInstitution(@Valid @RequestBody InstitutionLoginDTO loginDTO) {
        try {
            // Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

            // Fetch institution and generate JWT
            Institution institution = institutionService.login(loginDTO);
            String token = jwtUtil.generateToken(institutionService.loadUserByUsername(loginDTO.getEmail()));
            InstitutionLoginDTO institutionDTO = InstitutionService.mapToLoginDTO(institution);

            // Return token and institution details
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "institution", institutionDTO));
        } catch (BadCredentialsException e) {
            // Return 401 for invalid credentials
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // Fallback error handler for uncaught exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}
