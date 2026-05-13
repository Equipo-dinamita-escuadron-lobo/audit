package com.audit.infrastructure.adapters.output.exception.security;

public class AuditMappingException extends RuntimeException {

    public AuditMappingException(String message) {
        super(message);
    }

    public AuditMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
