package com.audit.infrastructure.adapters.output.exception.security;

public class ExportException extends RuntimeException {
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
