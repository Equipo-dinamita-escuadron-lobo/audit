package com.audit.application.port.input.commands;

import com.audit.application.dto.request.LogSessionRequest;

/** 
 * @brief input port for session event logger
 */
public interface LogAuditSessionCommand {

    //LogSessionResponse executeSync(LogSessionRequest request);

    void executeAsync(LogSessionRequest request);
}
