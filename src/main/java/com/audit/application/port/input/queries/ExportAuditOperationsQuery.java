package com.audit.application.port.input.queries;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.response.ExportFileResponse;

public interface ExportAuditOperationsQuery {

    ExportFileResponse execute(ExportOperationsRequest request);
}
