package com.zone01kisumu.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01kisumu.backend.dto.PasswordResetDTOs.ForgotPasswordRequest;
import com.zone01kisumu.backend.dto.PasswordResetDTOs.ResetPasswordRequest;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.service.OtpService;
import com.zone01kisumu.backend.service.UserLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserLookupService userLookupService;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private Student testStudent;
    private UserLookupService.UserLookupResult testUserResult;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(passwordResetController).build();

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setEmail("student@example.com");
        testStudent.setPhone("+1234567890");

        testUserResult = new UserLookupService.UserLookupResult(
                testStudent, "ROLE_STUDENT", "1"
        );
    }

    // ================ forgotPassword Tests ================

    @Test
    void forgotPassword_ShouldSendOtpViaEmail_WhenUserExistsWithEmail() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("student@example.com")
                .role("student")
                .build();

        when(userLookupService.findUserByPhoneOrEmail("student@example.com"))
                .thenReturn(Optional.of(testUserResult));
        when(userLookupService.isEmail("student@example.com")).thenReturn(true);
        doNothing().when(otpService).sendOtp("student@example.com", "email", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists, a reset code has been sent."));

        verify(userLookupService).findUserByPhoneOrEmail("student@example.com");
        verify(userLookupService).isEmail("student@example.com");
                verify(otpService).sendOtp("student@example.com", "email", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);
    }

    @Test
    void forgotPassword_ShouldSendOtpViaSms_WhenUserExistsWithPhone() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("+1234567890")
                .role("student")
                .build();

        when(userLookupService.findUserByPhoneOrEmail("+1234567890")).thenReturn(Optional.of(testUserResult));
        when(userLookupService.isEmail("+1234567890")).thenReturn(false);
        doNothing().when(otpService).sendOtp("+1234567890", "sms", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists, a reset code has been sent."));

        verify(userLookupService).findUserByPhoneOrEmail("+1234567890");
        verify(userLookupService).isEmail("+1234567890");
                verify(otpService).sendOtp("+1234567890", "sms", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);
    }

    @Test
    void forgotPassword_ShouldReturnSuccessWithoutSendingOtp_WhenUserNotFound() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("unknown@example.com")
                .role("student")
                .build();

        when(userLookupService.findUserByPhoneOrEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists, a reset code has been sent."));

        verify(userLookupService).findUserByPhoneOrEmail("unknown@example.com");
                verify(otpService, never()).sendOtp(anyString(), anyString(), any());
    }

    @Test
    void forgotPassword_ShouldReturnBadRequest_WhenPhoneOrEmailIsMissing() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("")
                .role("student")
                .build();

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userLookupService, never()).findUserByPhoneOrEmail(anyString());
                verify(otpService, never()).sendOtp(anyString(), anyString(), any());
    }

    @Test
    void forgotPassword_ShouldReturnBadRequest_WhenRoleIsMissing() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("student@example.com")
                .role("")
                .build();

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userLookupService, never()).findUserByPhoneOrEmail(anyString());
                verify(otpService, never()).sendOtp(anyString(), anyString(), any());
    }

    @Test
    void forgotPassword_ShouldReturnServerError_WhenServiceThrowsException() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .phoneOrEmail("student@example.com")
                .role("student")
                .build();

        when(userLookupService.findUserByPhoneOrEmail("student@example.com"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to process request"));
    }

    // ================ resetPassword Tests ================

    @Test
    void resetPassword_ShouldReturnSuccess_WhenOtpIsValidAndUserExists() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("student@example.com")
                .otpCode("123456")
                .newPassword("newSecurePass123")
                .role("student")
                .build();

        when(otpService.verifyOtp("student@example.com", "123456", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET)).thenReturn(true);
        when(userLookupService.findUserByPhoneOrEmail("student@example.com"))
                .thenReturn(Optional.of(testUserResult));
        when(passwordEncoder.encode("newSecurePass123")).thenReturn("encodedPassword123");
        doNothing().when(userLookupService).updateUserPassword(testStudent, "encodedPassword123");

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Password reset successful. You can now log in with your new password."));

        verify(otpService).verifyOtp("student@example.com", "123456", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);
        verify(userLookupService).findUserByPhoneOrEmail("student@example.com");
        verify(passwordEncoder).encode("newSecurePass123");
        verify(userLookupService).updateUserPassword(testStudent, "encodedPassword123");
    }

    @Test
    void resetPassword_ShouldReturnSuccess_WhenPhoneUsedAndUserExists() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("+1234567890")
                .otpCode("654321")
                .newPassword("newSecurePass123")
                .role("student")
                .build();

        when(otpService.verifyOtp("+1234567890", "654321", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET)).thenReturn(true);
        when(userLookupService.findUserByPhoneOrEmail("+1234567890")).thenReturn(Optional.of(testUserResult));
        when(passwordEncoder.encode("newSecurePass123")).thenReturn("encodedPassword123");
        doNothing().when(userLookupService).updateUserPassword(testStudent, "encodedPassword123");

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Password reset successful. You can now log in with your new password."));

        verify(otpService).verifyOtp("+1234567890", "654321", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);
        verify(userLookupService).findUserByPhoneOrEmail("+1234567890");
        verify(passwordEncoder).encode("newSecurePass123");
        verify(userLookupService).updateUserPassword(testStudent, "encodedPassword123");
    }

    @Test
    void resetPassword_ShouldReturnBadRequest_WhenOtpIsInvalidOrExpired() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("student@example.com")
                .otpCode("000000")
                .newPassword("newSecurePass123")
                .role("student")
                .build();

        when(otpService.verifyOtp("student@example.com", "000000", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET)).thenReturn(false);

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired code"));

        verify(otpService).verifyOtp("student@example.com", "000000", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);
        verify(userLookupService, never()).findUserByPhoneOrEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userLookupService, never()).updateUserPassword(any(), anyString());
    }

    @Test
    void resetPassword_ShouldReturnBadRequest_WhenUserNotFoundAfterValidOtp() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("unknown@example.com")
                .otpCode("123456")
                .newPassword("newSecurePass123")
                .role("student")
                .build();

        when(otpService.verifyOtp("unknown@example.com", "123456", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET)).thenReturn(true);
        when(userLookupService.findUserByPhoneOrEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No account found"));

        verify(otpService).verifyOtp("unknown@example.com", "123456", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET);
        verify(userLookupService).findUserByPhoneOrEmail("unknown@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userLookupService, never()).updateUserPassword(any(), anyString());
    }

    @Test
    void resetPassword_ShouldReturnBadRequest_WhenMissingFields() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("")
                .otpCode("")
                .newPassword("")
                .role("")
                .build();

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(otpService, never()).verifyOtp(anyString(), anyString());
    }

    @Test
    void resetPassword_ShouldReturnServerError_WhenExceptionThrown() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneOrEmail("student@example.com")
                .otpCode("123456")
                .newPassword("newSecurePass123")
                .role("student")
                .build();

        when(otpService.verifyOtp("student@example.com", "123456", com.zone01kisumu.backend.service.OtpService.Purpose.PASSWORD_RESET))
                .thenThrow(new RuntimeException("OTP service failure"));

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to reset password"));
    }
}
