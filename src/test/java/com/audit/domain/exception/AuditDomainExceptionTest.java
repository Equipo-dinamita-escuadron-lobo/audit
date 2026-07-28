package com.audit.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.exceptions.InvalidAuditEventException;

import static org.junit.jupiter.api.Assertions.*;

class AuditDomainExceptionTest {

    @Test
    @DisplayName("InvalidAuditEventException - debe conservar mensaje")
    void invalidAuditEventException_message() {
        InvalidAuditEventException exception = new InvalidAuditEventException("Error de auditoría");

        assertEquals("Error de auditoría", exception.getMessage());
    }

    @Test
    @DisplayName("InvalidAuditEventException - debe conservar causa")
    void invalidAuditEventException_cause() {
        RuntimeException cause = new RuntimeException("Causa original");

        InvalidAuditEventException exception = new InvalidAuditEventException("Error de auditoría", cause);

        assertAll(
                () -> assertEquals("Error de auditoría", exception.getMessage()),
                () -> assertEquals(cause, exception.getCause()));
    }
}
