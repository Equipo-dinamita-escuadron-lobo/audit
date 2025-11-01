package com.audit.application.port.input.queries;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.responses.ExportSessionsResponse;

/**
 * @brief input port for exporting session events
 */
public interface ExportAuditSessionsQuery {

    ExportSessionsResponse execute(ExportSessionsRequest request);

}
