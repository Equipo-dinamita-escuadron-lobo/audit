package com.audit.application.internal;

import com.audit.domain.enums.UserRole;
import com.audit.domain.exceptions.InvalidAuditEventException;

public class AccessValidator {
    
    public static void validateSessionAccess(UserRole requestingRole) {
        if (requestingRole == UserRole.ESTUDIANTE) {
            throw new InvalidAuditEventException(
                "Students are not allowed to access");
        }
    }

    public static void validateOperationAccess(UserRole requestingRole, String enterpriseId) {
        if (requestingRole == UserRole.ESTUDIANTE && enterpriseId == null) {
            throw new InvalidAuditEventException(
                "Students cannot access system-level operation audit logs");
        }
    }
}
