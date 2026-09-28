
package com.zone01kisumu.backend.exceptionTests;

import com.zone01kisumu.backend.exception.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiErrorResponseTest {

    @Test
    void testNoArgsConstructor() {
        ApiErrorResponse errorResponse = new ApiErrorResponse();
        assertNotNull(errorResponse);
    }

    @Test
    void testAllArgsConstructor() {
        long timestamp = System.currentTimeMillis();
        ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Invalid input", timestamp);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid input", errorResponse.getMessage());
        assertEquals(timestamp, errorResponse.getTimestamp());
    }

    @Test
    void testConstructorWithoutTimestamp() {
        ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", "Resource not found");
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("Resource not found", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        ApiErrorResponse errorResponse = new ApiErrorResponse();
        long timestamp = System.currentTimeMillis();

        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setError("Internal Server Error");
        errorResponse.setMessage("Something went wrong");
        errorResponse.setTimestamp(timestamp);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("Something went wrong", errorResponse.getMessage());
        assertEquals(timestamp, errorResponse.getTimestamp());
    }
}
