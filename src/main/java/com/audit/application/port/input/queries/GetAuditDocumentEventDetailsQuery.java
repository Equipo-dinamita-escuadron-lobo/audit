package com.audit.application.port.input.queries;

import java.util.List;

import com.audit.application.dto.request.GetDocumentEventDetailRequest;
import com.audit.application.dto.response.DocumentEventDetailResponse;

public interface GetAuditDocumentEventDetailsQuery {
    List<DocumentEventDetailResponse> execute(GetDocumentEventDetailRequest request);
}
