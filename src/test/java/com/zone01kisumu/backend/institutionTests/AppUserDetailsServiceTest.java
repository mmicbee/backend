package com.zone01kisumu.backend.institutionTests;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.zone01kisumu.backend.service.AppUserDetailsService;

import static org.junit.jupiter.api.Assertions.*;

class AppUserDetailsServiceTest {

    private final AppUserDetailsService service = new AppUserDetailsService();

    @Test
    void loadUserByUsername_shouldReturnUserWithCorrectUsernameAndRoles() {
        String username = "testuser";

        UserDetails userDetails = service.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
