package com.audit.application.port.input.commands;

import com.audit.application.dto.request.LogDocumentEventRequest;

public interface LogAuditDocumentEventCommand {
    void execute(LogDocumentEventRequest request);
}
