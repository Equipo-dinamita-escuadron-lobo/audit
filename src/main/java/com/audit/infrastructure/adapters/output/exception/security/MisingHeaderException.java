package com.audit.infrastructure.adapters.output.exception.security;

public class MisingHeaderException extends RuntimeException {
    public MisingHeaderException(String message) {
        super(message);
    }

}
