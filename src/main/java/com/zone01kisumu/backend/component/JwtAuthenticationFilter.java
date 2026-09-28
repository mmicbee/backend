package com.zone01kisumu.backend.component;

import java.io.IOException;
import java.util.List;

import com.zone01kisumu.backend.util.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Filter to authenticate requests using JWT token
//Skips authentication for /api/auth/* endpoints
//Extracts and validates JWT, then sets authentication context
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Extract Authorization header
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Skip filtering for auth-related endpoints when no Authorization header is present
        String path = request.getServletPath();
        if (authorizationHeader == null && path != null
                && (path.startsWith("/api/auth/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/oauth2/"))) {
            LoggerUtil.logDebug("Skipping JWT authentication for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token if present
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                LoggerUtil.logDebug("Extracted username from JWT: {}", username);
            } catch (Exception e) {
                LoggerUtil.logError("Invalid JWT token for path {}: {}", path, e.getMessage());
                LoggerUtil.logError("JWT token extraction failed", e);
            }
        } else {
            LoggerUtil.logDebug("No valid Authorization header found for path: {}", path);
        }

        // Validate token and set authentication if not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String role = jwtUtil.extractRole(jwt);
                String normalizedRole = (role != null && role.toUpperCase().startsWith("ROLE_"))
                        ? role.toUpperCase()
                        : "ROLE_" + (role != null ? role.toUpperCase() : "STUDENT");
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(normalizedRole));

                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        username, "", authorities);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    LoggerUtil.logInfo("Successfully authenticated user: {} with role: {} for path: {}",
                            username, role, path);
                } else {
                    LoggerUtil.logWarn("JWT token validation failed for user: {} on path: {}", username, path);
                }
            } catch (Exception e) {
                LoggerUtil.logError("Authentication error for user {} on path {}: {}", username, path, e.getMessage());
                LoggerUtil.logError("Authentication processing failed", e);
            }
        } else if (username == null && authorizationHeader != null) {
            LoggerUtil.logWarn("Failed to extract username from JWT for path: {}", path);
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
