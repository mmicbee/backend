package com.zone01kisumu.backend.configTests;
import com.zone01kisumu.backend.config.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {

    @Test
    void addCorsMappings_shouldConfigureCorsCorrectly() throws Exception {
        // Arrange
        WebConfig config = new WebConfig();
        TestCorsRegistry registry = new TestCorsRegistry();

        // Act
        config.addCorsMappings(registry);

        // Assert
        assertTrue(registry.wasConfigured);
        assertTrue(registry.origins.contains("http://localhost:*"));
        assertTrue(registry.origins.contains("https://lms-ujuzi.vercel.app"));
        assertEquals(List.of("*"), registry.methods);
        assertEquals("*", registry.headers);
        assertTrue(registry.credentials);
    }

    @Test
    void addCorsMappings_shouldIncludeCustomAllowedOrigins() {
        WebConfig config = new WebConfig();
        config.setAllowedOrigins("https://example.com, https://another.com");
        TestCorsRegistry registry = new TestCorsRegistry();

        config.addCorsMappings(registry);

        assertTrue(registry.wasConfigured);
        assertTrue(registry.origins.contains("https://example.com"));
        assertTrue(registry.origins.contains("https://another.com"));
        assertFalse(registry.origins.contains("https://lms-ujuzi.vercel.app"));
    }

    // Fake CorsRegistry for tracking calls
    static class TestCorsRegistry extends CorsRegistry {
        boolean wasConfigured = false;
        List<String> origins;
        List<String> methods;
        String headers;
        boolean credentials;

        @Override
        public CorsRegistration addMapping(String pathPattern) {
            assertEquals("/**", pathPattern);
            wasConfigured = true;
            return new CorsRegistration(pathPattern) {
                @Override
                public CorsRegistration allowedOriginPatterns(String... patterns) {
                    TestCorsRegistry.this.origins = Arrays.asList(patterns);
                    return this;
                }

                @Override
                public CorsRegistration allowedMethods(String... methods) {
                    TestCorsRegistry.this.methods = Arrays.asList(methods);
                    return this;
                }

                @Override
                public CorsRegistration allowedHeaders(String... headers) {
                    TestCorsRegistry.this.headers = headers.length > 0 ? headers[0] : null;
                    return this;
                }

                @Override
                public CorsRegistration allowCredentials(boolean allowCredentials) {
                    TestCorsRegistry.this.credentials = allowCredentials;
                    return this;
                }
            };
        }
    }
}
