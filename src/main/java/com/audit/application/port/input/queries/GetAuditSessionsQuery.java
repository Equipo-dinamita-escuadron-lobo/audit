package com.audit.application.port.input.queries;

import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.dto.response.SessionAuditResponse;

/**
 * @brief input port for querying session events
 */
public interface GetAuditSessionsQuery {

    PageResponse<SessionAuditResponse> execute(GetSessionsRequest request);
}
