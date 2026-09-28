package com.zone01kisumu.backend.component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

//jwt util component for token generation
@Component
public class JwtUtil {

    // JWT secret key loaded from application properties or default
    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    // Token expiration time in milliseconds (default 24 hours)
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private static final long CLOCK_SKEW_SECONDS = 0;

    // Generate JWT token with username and role
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", normalizeRole(role));
        return createToken(claims, username);
    }

    // Generate JWT token from UserDetails
    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().isEmpty()
                ? "ROLE_STUDENT"
                : userDetails.getAuthorities().iterator().next().getAuthority();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", normalizeRole(role));
        return createToken(claims, userDetails.getUsername());
    }

    // Helper to normalize role to canonical ROLE_<UPPERCASE>
    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "ROLE_STUDENT";
        }
        String upper = role.trim().toUpperCase();
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }

    // Create JWT token with claims and subject
    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date()) // token creation time
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // token expiry time
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract role claim from JWT token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Extract username (subject) from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract any claim using a claims resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .setAllowedClockSkewSeconds(CLOCK_SKEW_SECONDS)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Validate token matches user details and is not expired
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Get signing key from secret
    private Key getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Setters for testing purposes
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}