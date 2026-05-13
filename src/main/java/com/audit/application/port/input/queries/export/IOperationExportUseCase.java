package com.audit.application.port.input.queries.export;

import com.audit.application.dto.request.ExportOperationsRequest;

public interface IOperationExportUseCase {

    String execute(ExportOperationsRequest request);
}
