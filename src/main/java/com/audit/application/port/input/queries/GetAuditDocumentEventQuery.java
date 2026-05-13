package com.audit.application.port.input.queries;

import com.audit.application.dto.request.GetDocumentsEventsRequest;
import com.audit.application.dto.response.DocumentEventAuditResponse;
import com.audit.application.dto.response.PageResponse;

public interface GetAuditDocumentEventQuery {
    PageResponse<DocumentEventAuditResponse> execute(GetDocumentsEventsRequest request);
}
