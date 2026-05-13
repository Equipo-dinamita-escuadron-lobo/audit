package com.audit.application.port.output;

import com.audit.application.dto.request.ExportOperationsRequest;

public interface IOperationAsyncExportPort {

    void process(ExportOperationsRequest request, String jobId);
}
