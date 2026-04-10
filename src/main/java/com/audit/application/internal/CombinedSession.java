package com.audit.application.internal;

import java.time.Instant;

import com.audit.domain.enums.UserRole;

import lombok.Getter;

@Getter
public class CombinedSession {
    private final String sessionId;
    private final String userName;
    private final UserRole userRole;
    private final Instant loginTime;
    private final Instant logoutTime;

    private CombinedSession(String sessionId, String userName,
            UserRole userRole, Instant loginTime, Instant logoutTime) {
        this.sessionId = sessionId;
        this.userName = userName;
        this.userRole = userRole;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
    }

    public static CombinedSession of(String sessionId, String userName,
            UserRole userRole, Instant loginTime, Instant logoutTime) {
        if (sessionId == null || sessionId.isBlank())
            throw new IllegalArgumentException("Session ID required");
        if (loginTime == null)
            throw new IllegalArgumentException("Login time required");
        return new CombinedSession(sessionId, userName, userRole, loginTime, logoutTime);
    }

    public boolean isActive() {
        return logoutTime == null;
    }
}
