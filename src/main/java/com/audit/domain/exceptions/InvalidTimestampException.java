package com.audit.domain.exceptions;

public class InvalidTimestampException extends AuditDomainException {

    private final String invalidTimestamp;

    public InvalidTimestampException(String invalidTimestamp) {
        super(String.format("Invalid timestamp provided: %s. Timestamp cannot be null or in the future",
                invalidTimestamp));
        this.invalidTimestamp = invalidTimestamp;
    }

    public String getInvalidTimestamp() {
        return invalidTimestamp;
    }
    
}
