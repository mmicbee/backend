
package com.zone01kisumu.backend.exceptionTests;

import com.zone01kisumu.backend.exception.ApiErrorResponse;
import com.zone01kisumu.backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleAuthenticationExceptions_BadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");
        ResponseEntity<ApiErrorResponse> responseEntity = globalExceptionHandler.handleAuthenticationExceptions(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        ApiErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), errorResponse.getError());
        assertEquals("Invalid email or password.", errorResponse.getMessage());
    }

    @Test
    void handleAuthenticationExceptions_UsernameNotFound() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");
        ResponseEntity<ApiErrorResponse> responseEntity = globalExceptionHandler.handleAuthenticationExceptions(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        ApiErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), errorResponse.getError());
        assertEquals("Invalid email or password.", errorResponse.getMessage());
    }

    @Test
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ResponseEntity<ApiErrorResponse> responseEntity = globalExceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        ApiErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), errorResponse.getError());
        assertEquals("You do not have permission to access this resource.", errorResponse.getMessage());
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("User already exists");
        ResponseEntity<ApiErrorResponse> responseEntity = globalExceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        ApiErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), errorResponse.getError());
        assertEquals("User already exists", errorResponse.getMessage());
    }

    @Test
    void handleAllUncaughtException() {
        Exception ex = new Exception("Something went wrong");
        ResponseEntity<ApiErrorResponse> responseEntity = globalExceptionHandler.handleAllUncaughtException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        ApiErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), errorResponse.getError());
        assertEquals("An unexpected error occurred: Something went wrong", errorResponse.getMessage());
    }
}
