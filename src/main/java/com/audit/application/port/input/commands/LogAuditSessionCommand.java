package com.audit.application.port.input.commands;

import com.audit.application.dto.request.LogSessionRequest;

/** 
 * @brief input port for session event logger
 */
public interface LogAuditSessionCommand {

    void execute(LogSessionRequest request);
}
