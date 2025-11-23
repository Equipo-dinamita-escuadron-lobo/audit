package com.audit.application.port.input.queries;

import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.PageResponse;

public interface GetAuditOperationsQuery {

    PageResponse<OperationAuditResponse> execute(GetOperationsRequest request);
}
