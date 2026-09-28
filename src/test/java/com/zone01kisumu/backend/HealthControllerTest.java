package com.zone01kisumu.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zone01kisumu.backend.controller.HealthController;

public class HealthControllerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void healthCheck_shouldReturnHealthyStatus() {
        doNothing().when(jdbcTemplate).execute("SELECT 1");

        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("database", "UP");
        assertThat(response.getBody()).containsEntry("service", "UP");
        assertThat(response.getBody()).containsKey("timestamp");
        verify(jdbcTemplate, times(1)).execute("SELECT 1");
    }

    @Test
    void healthCheck_shouldReturnServiceUnavailableWhenDatabaseFails() {
        doThrow(new RuntimeException("DB error")).when(jdbcTemplate).execute("SELECT 1");

        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("database", "DOWN");
        assertThat(response.getBody()).containsEntry("service", "UP");
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody()).containsKey("timestamp");
        verify(jdbcTemplate, times(1)).execute("SELECT 1");
    }

    @Test
    void root_shouldReturnUpStatusAndFrontendUrl() {
        ResponseEntity<Map<String, Object>> response = healthController.root();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
        assertThat(response.getBody()).containsEntry("service", "Ujuzi360 LMS Backend API");
        assertThat(response.getBody()).containsEntry("frontendUrl", "http://localhost:5173");
    }
}
