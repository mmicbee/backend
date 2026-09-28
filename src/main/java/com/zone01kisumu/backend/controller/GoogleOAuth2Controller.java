package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.component.JwtUtil;
import com.zone01kisumu.backend.model.Institution;
import com.zone01kisumu.backend.model.Student;
import com.zone01kisumu.backend.model.Teacher;
import com.zone01kisumu.backend.service.InstitutionService;
import com.zone01kisumu.backend.service.StudentAuthService;
import com.zone01kisumu.backend.service.TeacherAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class GoogleOAuth2Controller {

    private final StudentAuthService studentAuthService;
    private final TeacherAuthService teacherAuthService;
    private final InstitutionService institutionService;
    private final JwtUtil jwtUtil;
    
    @Value("${frontend.url:frontend.url}")
    private String frontendUrl;

    @Value("${frontend.oauth.callback.full-url:${frontend.url}/oauth/callback}")
    private String oauthCallbackFullUrl;

    public GoogleOAuth2Controller(
            StudentAuthService studentAuthService,
            TeacherAuthService teacherAuthService,
            InstitutionService institutionService,
            JwtUtil jwtUtil
    ) {
        this.studentAuthService = studentAuthService;
        this.teacherAuthService = teacherAuthService;
        this.institutionService = institutionService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/google/{role}")
    public void googleLogin(
            @PathVariable String role,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        request.getSession().setAttribute("role", role);
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/user")
    public void user(
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl;
        try {
           
            
            String role = (String) request.getSession().getAttribute("role");
            String email = principal.getAttribute("email");

            String token;
            String userId;

            if ("student".equals(role)) {
                Student student = studentAuthService.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException(
                                ""));
                token = jwtUtil.generateToken(student.getEmail(), "STUDENT");
                userId = student.getId().toString();
            } else if ("teacher".equals(role)) {
                Teacher teacher = teacherAuthService.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException(
                                ""));
                token = jwtUtil.generateToken(teacher.getEmail(), "TEACHER");
                userId = teacher.getId().toString();
            } else if ("institution".equals(role)) {
                Institution institution = institutionService.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException(
                                ""));
                token = jwtUtil.generateToken(institution.getEmail(), "INSTITUTION");
                userId = institution.getId().toString();
            } else {
                throw new IllegalArgumentException("Invalid role specified");
            }


            redirectUrl = UriComponentsBuilder.fromUriString(oauthCallbackFullUrl)
                    .queryParam("token", token)
                    .queryParam("userId", userId)
                    .queryParam("role", role)
                    .build().toUriString();

        } catch (Exception e) {
          
            e.printStackTrace();
            
            redirectUrl = UriComponentsBuilder.fromUriString(oauthCallbackFullUrl)
                    .queryParam("error", e.getMessage())
                    .build().toUriString();
        }
        
        // Clear the role from session
        request.getSession().removeAttribute("role");
        
        response.sendRedirect(redirectUrl);
    }
}