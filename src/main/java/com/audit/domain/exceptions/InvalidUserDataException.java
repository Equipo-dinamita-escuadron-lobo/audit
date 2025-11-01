package com.audit.domain.exceptions;

public class InvalidUserDataException extends AuditDomainException {
    private final String userId;

    public InvalidUserDataException(String userId, String message) {
        super(String.format("Invalid user data for user ID %d: %s", userId, message));
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
