package com.zone01kisumu.backend.service;
import com.zone01kisumu.backend.model.VirtualClassroomSession;

public interface VirtualClassroomService {
    VirtualClassroomSession createSession(String hostId);
    boolean joinSession(String sessionId, String userId);
}
