package com.zone01kisumu.backend.config;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01kisumu.backend.component.JwtAuthenticationFilter;
import com.zone01kisumu.backend.exception.ApiErrorResponse;
import com.zone01kisumu.backend.service.InstitutionService;
import com.zone01kisumu.backend.service.StudentAuthService;
import com.zone01kisumu.backend.service.TeacherAuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final PasswordEncoder passwordEncoder;
        private final StudentAuthService studentAuthService;
        private final TeacherAuthService teacherAuthService;
        private final InstitutionService institutionService;
        private final ObjectMapper objectMapper;

        @Value("${cors.allowedOrigins:http://localhost:5173,http://localhost:3000,https://lms-ujuzi.vercel.app}")
        private String allowedOrigins = "http://localhost:5173,http://localhost:3000,https://lms-ujuzi.vercel.app";

        public void setAllowedOrigins(String allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
        }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

                authBuilder.userDetailsService(studentAuthService).passwordEncoder(passwordEncoder);
                authBuilder.userDetailsService(teacherAuthService).passwordEncoder(passwordEncoder);
                authBuilder.userDetailsService(institutionService).passwordEncoder(passwordEncoder);

                return authBuilder.build();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> {})
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                // Public Auth, Swagger, Health, and Webhook endpoints
                                                .requestMatchers(
                                                                "/",
                                                                "/api/auth/student/register",
                                                                "/api/auth/student/login",
                                                                "/api/auth/teacher/register",
                                                                "/api/auth/teacher/login",
                                                                "/api/auth/institution/register",
                                                                "/api/auth/institution/login",
                                                                "/api/auth/login/**",
                                                                "/api/auth/google/**",
                                                                "/api/auth/password/**",
                                                                "/api/auth/user",
                                                                "/api/auth/oauth/token",
                                                                "/login/oauth2/**",
                                                                "/oauth2/**",
                                                                "/h2-console/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/healthz",
                                                                "/api/payments/webhook",
                                                                "/api/mpesa/callback",
                                                                "/api/mpesa/timeout")
                                                .permitAll()
                                                // Public Course & Topic Read Access
                                                .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/**")
                                                .permitAll()
                                                // Public Stored Course Media & Files
                                                .requestMatchers(HttpMethod.GET, "/api/storage", "/api/storage/**")
                                                .permitAll()
                                                // Course, Topic, and Lesson Management (Teacher and Institution)
                                                .requestMatchers(HttpMethod.POST, "/api/courses", "/api/courses/**")
                                                .hasAnyRole("TEACHER", "INSTITUTION")
                                                .requestMatchers(HttpMethod.PUT, "/api/courses", "/api/courses/**")
                                                .hasAnyRole("TEACHER", "INSTITUTION")
                                                .requestMatchers(HttpMethod.DELETE, "/api/courses", "/api/courses/**")
                                                .hasAnyRole("TEACHER", "INSTITUTION")
                                                // Course Enrollment (Students create enrollment, Teachers view)
                                                .requestMatchers(HttpMethod.POST, "/api/enrollments")
                                                .hasRole("STUDENT")
                                                .requestMatchers("/api/admin/monitoring/**")
                                                .hasAnyRole("TEACHER", "INSTITUTION")
                                                .requestMatchers("/api/reports/**")
                                                .authenticated()
                                                // Student Profile Management (Student role required)
                                                .requestMatchers("/api/student/**")
                                                .hasRole("STUDENT")
                                                .requestMatchers("/api/students/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/enrollments/teacher/**")
                                                .hasAnyRole("TEACHER", "INSTITUTION")
                                                .requestMatchers("/api/enrollments/**")
                                                .authenticated()
                                                // Course Progress & Attendance
                                                .requestMatchers(HttpMethod.POST, "/api/progress/mark-attendance")
                                                .hasAnyRole("STUDENT", "TEACHER")
                                                .requestMatchers("/api/progress/**")
                                                .authenticated()
                                                // Admin Backups & Disaster Recovery
                                                .requestMatchers("/api/admin/backups/**")
                                                .hasAnyRole("TEACHER", "INSTITUTION")
                                                // Quiz Scores
                                                .requestMatchers("/api/quizzes/**")
                                                .authenticated()
                                                // Payments & M-Pesa (authenticated users)
                                                .requestMatchers("/api/payments/**", "/api/mpesa/**")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                // IMPORTANT: Use IF_REQUIRED for OAuth to work with sessions
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .addFilterBefore(jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .oauth2Login(oauth2 -> oauth2
                                                .defaultSuccessUrl("/api/auth/user", true))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.disable()))
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        ApiErrorResponse errorResponse = new ApiErrorResponse(
                                                                        HttpStatus.UNAUTHORIZED.value(),
                                                                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                                                        "Invalid email or password.");

                                                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                                                        OutputStream out = response.getOutputStream();
                                                        objectMapper.writeValue(out, errorResponse);
                                                        out.flush();
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        ApiErrorResponse errorResponse = new ApiErrorResponse(
                                                                        HttpStatus.FORBIDDEN.value(),
                                                                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                                                                        "Access denied: insufficient permissions.");

                                                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                                                        OutputStream out = response.getOutputStream();
                                                        objectMapper.writeValue(out, errorResponse);
                                                        out.flush();
                                                }));

                return http.build();
        }

        private List<String> resolveAllowedOriginPatterns() {
                Set<String> patterns = new LinkedHashSet<>();
                patterns.add("http://localhost:*");
                patterns.add("http://127.0.0.1:*");

                if (allowedOrigins != null && !allowedOrigins.isBlank()) {
                        for (String origin : allowedOrigins.split(",")) {
                                String trimmed = origin.trim();
                                if (!trimmed.isEmpty()) {
                                        patterns.add(trimmed);
                                }
                        }
                }
                return new ArrayList<>(patterns);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOriginPatterns(resolveAllowedOriginPatterns());
                configuration.setAllowedMethods(
                                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Total-Count"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source =
                                new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
