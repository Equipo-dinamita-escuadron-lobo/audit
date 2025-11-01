package com.audit.application.port.output;

import java.util.List;

import com.audit.application.dto.responses.SessionAuditResponse;

public interface FileExportService {
    byte[] exportSessions(List<SessionAuditResponse> sessions, String format);
}
