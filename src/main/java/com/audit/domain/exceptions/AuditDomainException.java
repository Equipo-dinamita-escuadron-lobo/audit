package com.audit.domain.exceptions;

public abstract class AuditDomainException extends RuntimeException {
  
    protected AuditDomainException(String message) {
        super(message);
    }

    protected AuditDomainException(String message, Throwable cause) {
        super(message, cause);
    }

}
