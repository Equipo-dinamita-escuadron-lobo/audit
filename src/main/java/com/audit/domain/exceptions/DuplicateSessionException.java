package com.audit.domain.exceptions;

public class DuplicateSessionException extends AuditDomainException {

    private final String userId;

    public DuplicateSessionException(String userId) {
        super(String.format("Duplicate session detected for user ID: %s", userId));
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

}
