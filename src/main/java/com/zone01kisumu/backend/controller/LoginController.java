package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.dto.*;
import com.zone01kisumu.backend.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth/login")
@RequiredArgsConstructor
public class LoginController {

    private final StudentAuthService studentAuthService;
    private final TeacherAuthService teacherAuthService;
    private final InstitutionService institutionService;
    private final UserLookupService userLookupService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    @PostMapping("/credentials/student")
    public ResponseEntity<AuthResponse> loginStudent(@Valid @RequestBody StudentLogin request) {
        final var student = studentAuthService.loginStudent(request);
        final String jwtToken = jwtUtil.generateToken(student.getEmail(), "ROLE_STUDENT");

        final AuthResponse response = AuthResponse.builder()
                .message("Student login successful!")
                .userId(student.getId().toString())
                .token(jwtToken)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/credentials/teacher")
    public ResponseEntity<AuthResponse> loginTeacher(@Valid @RequestBody TeacherLogin request) {
        final var teacher = teacherAuthService.loginTeacher(request);
        final String jwtToken = jwtUtil.generateToken(teacher.getEmail(), "ROLE_TEACHER");

        final AuthResponse response = AuthResponse.builder()
                .message("Teacher login successful!")
                .userId(teacher.getId().toString())
                .token(jwtToken)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/credentials/institution")
    public ResponseEntity<AuthResponse> loginInstitution(@Valid @RequestBody InstitutionLoginDTO request) {
        final var institution = institutionService.login(request);
        final String jwtToken = jwtUtil.generateToken(institution.getEmail(), "ROLE_INSTITUTION");

        final AuthResponse response = AuthResponse.builder()
                .message("Institution login successful!")
                .userId(institution.getId().toString())
                .token(jwtToken)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/google/{role}")
    public void initiateGoogleLogin(
            @PathVariable String role,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        // Validate role
        if (!isValidRole(role)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role specified");
            return;
        }

        // Store role in session for OAuth callback
        request.getSession().setAttribute("role", role);
        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/otp/send")
    public ResponseEntity<AuthResponse> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        try {
            // Validate role
            if (!isValidRole(request.getRole())) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder().message("Invalid role specified").build());
            }

            // Check if user exists
            var userResult = userLookupService.findUserByPhoneOrEmail(request.getPhoneOrEmail());
            if (userResult.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .message("No account found with this phone or email. Please register first.")
                                .build());
            }

            // Determine OTP method
            String otpMethod = request.getOtpMethod();
            if (otpMethod == null) {
                otpMethod = userLookupService.isEmail(request.getPhoneOrEmail()) ? "email" : "sms";
            }

            // Send OTP
            otpService.sendOtp(request.getPhoneOrEmail(), otpMethod);

            return ResponseEntity.ok(AuthResponse.builder()
                    .message("OTP sent successfully via " + otpMethod)
                    .build());

        } catch (Exception e) {
            log.error("Error sending OTP: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder().message("Failed to send OTP").build());
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        try {
            // Validate role
            if (!isValidRole(request.getRole())) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder().message("Invalid role specified").build());
            }

            // Verify OTP
            boolean isValidOtp = otpService.verifyOtp(request.getPhoneOrEmail(), request.getOtpCode());
            if (!isValidOtp) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder().message("Invalid or expired OTP").build());
            }

            // Find user and generate JWT
            var userResult = userLookupService.findUserByPhoneOrEmail(request.getPhoneOrEmail());
            if (userResult.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .message("No account found with this phone or email")
                                .build());
            }

            var user = userResult.get();
            String email = userLookupService.getUserEmail(user.user);
            String jwtToken = jwtUtil.generateToken(email, user.userType);

            return ResponseEntity.ok(AuthResponse.builder()
                    .message("OTP verification successful!")
                    .userId(user.userId)
                    .token(jwtToken)
                    .build());

        } catch (Exception e) {
            log.error("Error verifying OTP: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder().message("Failed to verify OTP").build());
        }
    }

    private boolean isValidRole(String role) {
        return "student".equals(role) || "teacher".equals(role) || "institution".equals(role);
    }
}