package com.audit.domain.exceptions;

public class ExportLimitExceededException extends AuditDomainException {

    public ExportLimitExceededException(String message) {
        super(message);
    }

    public ExportLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

}
