package com.zone01kisumu.backend.model;

public class VirtualClassroomSession {
    private String sessionId;
    private String hostId;

    public VirtualClassroomSession(String sessionId, String hostId) {
        this.sessionId = sessionId;
        this.hostId = hostId;
    }

    public String getSessionId() { return sessionId; }
    public String getHostId() { return hostId; }
}
