package com.audit.domain.model;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserRole;

import lombok.Getter;

@Getter
public class CombinedSession {

    private final String sessionId;
    private final String userName;
    private final UserRole userRole;
    private final ZonedDateTime loginTime;
    private final ZonedDateTime logoutTime;

    private CombinedSession(String sessionId, String userName, UserRole userRole,
            ZonedDateTime loginTime, ZonedDateTime logoutTime) {
        this.sessionId = sessionId;
        this.userName = userName;
        this.userRole = userRole;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
    }

    public static CombinedSession create(String sessionId, String userName,
            UserRole userRole, ZonedDateTime loginTime,
            ZonedDateTime logoutTime) {

        validateSessionId(sessionId);
        validateUserData(userName, userRole);
        validateLoginTime(loginTime);

        return new CombinedSession(sessionId, userName, userRole, loginTime, logoutTime);
    }

    public static CombinedSession reconstruct(String sessionId, String userName,
            String userRoleStr, ZonedDateTime loginTime,
            ZonedDateTime logoutTime) {
        UserRole userRole = UserRole.valueOf(userRoleStr);
        return new CombinedSession(sessionId, userName, userRole, loginTime, logoutTime);
    }

    public boolean isActive() {
        return logoutTime == null;
    }


    private static void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
    }

    private static void validateUserData(String userName, UserRole userRole) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        if (userRole == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
    }

    private static void validateLoginTime(ZonedDateTime loginTime) {
        if (loginTime == null) {
            throw new IllegalArgumentException("Login time cannot be null");
        }
    }
}
