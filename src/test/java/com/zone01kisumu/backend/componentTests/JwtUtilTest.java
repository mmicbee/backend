package com.zone01kisumu.backend.componentTests;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.zone01kisumu.backend.component.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret("12345678901234567890123456789012");
        jwtUtil.setExpiration(1000L * 60 * 60); // 1 hour
    }

    @Test
    void testGenerateAndValidateToken() {
        UserDetails user = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));

        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertEquals("testuser", jwtUtil.extractUsername(token));
        assertFalse(jwtUtil.isTokenExpired(token));
        assertTrue(jwtUtil.validateToken(token, user));
    }

    @Test
    void testGenerateTokenWithUsernameAndRole() {
        String username = "simpleUser";
        String role = "STUDENT";

        String token = jwtUtil.generateToken(username, role);

        assertNotNull(token, "Token should not be null");
        assertEquals(username, jwtUtil.extractUsername(token), "Extracted username should match input");
        assertFalse(jwtUtil.isTokenExpired(token), "Token should not be expired immediately after generation");

        // Optional: Validate role claim (if you implement a method to extract it)
        String extractedRole = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("ROLE_STUDENT", extractedRole, "Role claim should be embedded in the token");
    }

    @Test
    void testGenerateTokenRoleNormalization() {
        // Test role normalization for Teacher without prefix
        String token1 = jwtUtil.generateToken("teacher1", "TEACHER");
        assertEquals("ROLE_TEACHER", jwtUtil.extractRole(token1));

        // Test role normalization for Teacher with prefix
        String token2 = jwtUtil.generateToken("teacher2", "ROLE_TEACHER");
        assertEquals("ROLE_TEACHER", jwtUtil.extractRole(token2));

        // Test role normalization for Institution
        String token3 = jwtUtil.generateToken("inst1", "INSTITUTION");
        assertEquals("ROLE_INSTITUTION", jwtUtil.extractRole(token3));

        // Test role normalization from UserDetails with ROLE_TEACHER
        UserDetails teacherUser = new User(
                "teacherUser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")));
        String token4 = jwtUtil.generateToken(teacherUser);
        assertEquals("ROLE_TEACHER", jwtUtil.extractRole(token4));

        // Test role normalization from UserDetails with ROLE_INSTITUTION
        UserDetails instUser = new User(
                "instUser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_INSTITUTION")));
        String token5 = jwtUtil.generateToken(instUser);
        assertEquals("ROLE_INSTITUTION", jwtUtil.extractRole(token5));
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret("12345678901234567890123456789012");
        jwtUtil.setExpiration(1L); // 1 millisecond

        UserDetails user = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));

        String token = jwtUtil.generateToken(user);

        Thread.sleep(100); // Wait to ensure token is expired

        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.extractClaim(token, Claims::getExpiration);
        });
    }

    @Test
void testIsTokenExpiredSafeCheck() throws InterruptedException {
    jwtUtil.setExpiration(1L);
    jwtUtil.setSecret("12345678901234567890123456789012");

    UserDetails user = new User(
            "testuser",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));

    String token = jwtUtil.generateToken(user);
    Thread.sleep(100); // Let it expire

    boolean expired;
    try {
        expired = jwtUtil.isTokenExpired(token);
    } catch (ExpiredJwtException e) {
        expired = true; // fallback: we know it's expired
    }

    assertTrue(expired, "Token should be considered expired");
}


}
