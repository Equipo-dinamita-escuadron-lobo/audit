package com.audit.application.port.input.commands;

import com.audit.application.dto.request.LogOperationRequest;

public interface LogAuditOperationCommand {
    void execute(LogOperationRequest request);
}
