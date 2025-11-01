package com.audit.application.port.input.queries;

import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.responses.SessionsPageResponse;

/**
 * @brief input port for querying session events
 */
public interface GetAuditSessionsQuery {

    SessionsPageResponse execute(GetSessionsRequest request);
}
