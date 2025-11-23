package com.audit.application.port.input.queries;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.response.ExportFileResponse;

/**
 * @brief input port for exporting session events
 */
public interface ExportAuditSessionsQuery {

    ExportFileResponse execute(ExportSessionsRequest request);

}
