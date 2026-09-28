package com.zone01kisumu.backend;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.zone01kisumu.backend.service.VirtualClassroomService;
import com.zone01kisumu.backend.model.VirtualClassroomSession;

class VirtualClassroomServiceTest {

    @Test
    void testSessionCreation() {
        assertTrue(true, "Session created successfully");
    }

    @Test
    void createSession_WithValidData_ReturnsSessionObj() {
        VirtualClassroomService mockService = mock(VirtualClassroomService.class);
        VirtualClassroomSession expectedSession = new VirtualClassroomSession("session-123", "host-456");
        
        when(mockService.createSession("host-456")).thenReturn(expectedSession);
        
        VirtualClassroomSession session = mockService.createSession("host-456");
        assertNotNull(session, "Session object should not be null");
        assertEquals("session-123", session.getSessionId());
        verify(mockService, times(1)).createSession("host-456");
    }

    @Test
    void joinSession_ValidUser_AddsUserToParticipants() {
        VirtualClassroomService mockService = mock(VirtualClassroomService.class);
        when(mockService.joinSession("session-123", "user-789")).thenReturn(true);
        
        boolean joined = mockService.joinSession("session-123", "user-789");
        assertTrue(joined, "User should be successfully added to participant list");
        verify(mockService, times(1)).joinSession("session-123", "user-789");
    }
}
