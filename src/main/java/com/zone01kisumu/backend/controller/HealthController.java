package com.zone01kisumu.backend.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Ujuzi360 LMS Backend API");
        response.put("frontendUrl", "http://localhost:5173");
        response.put("message", "Backend is running successfully. "
                + "Access the frontend application at http://localhost:5173");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/healthz")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        boolean dbHealthy = true;

        try {
            jdbcTemplate.execute("SELECT 1");
            healthStatus.put("database", "UP");
        } catch (Exception e) {
            dbHealthy = false;
            healthStatus.put("database", "DOWN");
            healthStatus.put("error", "Database connection failed: " + e.getMessage());
            logger.error("Database health check failed", e);
        }

        healthStatus.put("service", "UP");
        healthStatus.put("timestamp", new Date());

        if (dbHealthy) {
            logger.info("Health check passed at {}", new Date());
            return ResponseEntity.ok(healthStatus);
        } else {
            return ResponseEntity.status(503).body(healthStatus); // 503 = Service Unavailable
        }
    }
}
