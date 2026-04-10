package com.audit.domain.model;

import java.time.Instant;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;
import com.audit.domain.exceptions.InvalidAuditEventException;

import lombok.Getter;

/**
 * @brief Domain model representing an audit session event (login/logout)
 */
@Getter
public class AuditSession {

    // Attributes
    private final Long id;
    private final String sessionId;
    private final String userId;
    private final String userName;
    private final UserRole userRole;
    private final UserAction action;
    private final Instant actionAt;
    private final String ipAddress;
    private final Instant createdAt;

    // Constructor
    /**
     * Private constructor to enforce the use of the factory method
     */
    private AuditSession(Long id, String sessionId, String userId, String userName, UserRole userRole, UserAction action,
            Instant actionAt, String ipAddress, Instant createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.action = action;
        this.actionAt = actionAt;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    // Factory method
    /**
     * Factory method to create a new AuditSession instance with validation
     */
    public static AuditSession create(String sessionId, String userId, String userName, UserRole userRole, UserAction action,
            Instant actionAt, String ipAddress) {

        validateUserData(userId, userName, userRole);
        validateSessionAction(action);
        validateIPAddress(ipAddress);
        validateActionAt(actionAt);
        validateSessionId(sessionId);

        Instant now = Instant.now();

        return new AuditSession(null, sessionId, userId, userName, userRole, action, actionAt, ipAddress,
                now);
    }

    /**
     * Factory method to reconstruct an existing AuditSession instance
     */
    public static AuditSession reconstruct(Long id, String sessionId, String userId, String userName, UserRole userRole,
            UserAction action,
            Instant actionAt, String ipAddress, Instant createdAt) {

        return new AuditSession(id, sessionId, userId, userName, userRole, action, actionAt, ipAddress,
                createdAt);
    }

    // Private validation methods
    private static void validateUserData(String userId, String userName, UserRole userRole) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidAuditEventException("User ID cannot be null or empty");
        }

        if (userName == null || userName.trim().isEmpty()) {
            throw new InvalidAuditEventException("User name cannot be null or empty");
        }

        if (userName.trim().length() > 255) {
            throw new InvalidAuditEventException("User name cannot exceed 255 characters");
        }

        if (userRole == null) {
            throw new InvalidAuditEventException("User role cannot be null");
        }
    }

    private static void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new InvalidAuditEventException("Session ID cannot be null or empty");
        }
    }

    private static void validateSessionAction(UserAction action) {
        if (action != UserAction.LOGIN && action != UserAction.LOGOUT) {
            throw new InvalidAuditEventException(
                    String.format("Invalid session action: %s. Only LOGIN and LOGOUT are allowed.", action));
        }
    }

    private static void validateIPAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new InvalidAuditEventException("IP address cannot be null or empty");
        }
        String trimmedIp = ipAddress.trim();
        if (trimmedIp.length() > 45) {
            throw new InvalidAuditEventException("IP address format is invalid");
        }
    }

    private static void validateActionAt(Instant actionAt) {
        if (actionAt == null) {
            throw new InvalidAuditEventException("Action timestamp cannot be null");
        }
        if (actionAt.isAfter(Instant.now())) {
            throw new InvalidAuditEventException("Action timestamp cannot be in the future");
        }
    }
}