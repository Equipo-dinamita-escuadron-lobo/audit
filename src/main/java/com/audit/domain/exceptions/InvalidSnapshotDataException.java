package com.audit.domain.exceptions;

public class InvalidSnapshotDataException extends AuditDomainException {

    public InvalidSnapshotDataException(String message) {
        super(message);
    }

    public InvalidSnapshotDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
