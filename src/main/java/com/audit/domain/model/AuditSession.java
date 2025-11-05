package com.audit.domain.model;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;
import com.audit.domain.exceptions.InvalidAuditEventException;
import com.audit.domain.exceptions.InvalidTimestampException;
import com.audit.domain.exceptions.InvalidUserDataException;

import lombok.Getter;

import java.util.Objects;

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
    private final ZonedDateTime actionAt;
    private final String ipAddress;
    private final ZonedDateTime createdAt;

    // Constructor
    /**
     * Private constructor to enforce the use of the factory method
     */
    private AuditSession(Long id, String sessionId, String userId, String userName, UserRole userRole, UserAction action,
            ZonedDateTime actionAt, String ipAddress, ZonedDateTime createdAt) {
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
            ZonedDateTime actionAt, String ipAddress) {

        validateUserData(userId, userName, userRole);
        validateSessionAction(action);
        validateIPAddress(ipAddress);
        validateActionAt(actionAt);

        ZonedDateTime createdAt = ZonedDateTime.now();

        return new AuditSession(null, sessionId, userId, userName, userRole, action, actionAt, ipAddress,
                createdAt);
    }

    /**
     * Factory method to reconstruct an existing AuditSession instance
     */
    public static AuditSession reconstruct(Long id, String sessionId, String userId, String userName, UserRole userRole,
            UserAction action,
            ZonedDateTime actionAt, String ipAddress, ZonedDateTime createdAt) {

        return new AuditSession(id, sessionId, userId, userName, userRole, action, actionAt, ipAddress,
                createdAt);
    }

    // Private validation methods
    private static void validateUserData(String userId, String userName, UserRole userRole) {
        if (userId == null || userName.trim().isEmpty()) {
            throw new InvalidUserDataException(userId, "User ID cannot be null or empty");
        }

        if (userName == null || userName.trim().isEmpty()) {
            throw new InvalidUserDataException(userId, "User name cannot be null or empty");
        }

        if (userName.trim().length() > 255) {
            throw new InvalidUserDataException(userId, "User name cannot exceed 255 characters");
        }

        if (userRole == null) {
            throw new InvalidUserDataException(userId, "User role cannot be null");
        }
    }

    private static void validateSessionAction(UserAction action) {
        if (action == null) {
            throw new InvalidAuditEventException("Session action cannot be null");
        }
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

    private static void validateActionAt(ZonedDateTime actionAt) {
        if (actionAt == null) {
            throw new InvalidTimestampException("Action timestamp cannot be null");
        }
        if (actionAt.isAfter(ZonedDateTime.now())) {
            throw new InvalidTimestampException("Action timestamp cannot be in the future");
        }
    }

    // Business methods

    public boolean isLoginAction() {
        return this.action == UserAction.LOGIN;
    }

    public boolean isLogoutAction() {
        return this.action == UserAction.LOGOUT;
    }

    public boolean belongsToUser(String userId) {
        return Objects.equals(this.userId, userId);
    }

    // Overridden methods
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AuditSession that = (AuditSession) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(actionAt, that.actionAt) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, actionAt, action);
    }

}