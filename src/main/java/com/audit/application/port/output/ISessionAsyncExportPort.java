package com.audit.application.port.output;

import com.audit.application.dto.request.ExportSessionsRequest;

public interface ISessionAsyncExportPort {

    void process(ExportSessionsRequest request, String jobId);
}
