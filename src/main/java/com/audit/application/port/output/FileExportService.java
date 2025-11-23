package com.audit.application.port.output;

import java.util.List;

import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.SessionAuditResponse;

public interface FileExportService {
    byte[] exportSessions(List<SessionAuditResponse> sessions, String format);

    byte[] exportOperations(List<OperationAuditResponse> operations, String format);
}
