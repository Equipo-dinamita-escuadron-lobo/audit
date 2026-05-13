package com.audit.application.port.output;

import com.audit.application.dto.request.ExportDocumentsEventsRequest;

public interface IDocumentAsyncExportPort {

    void process(ExportDocumentsEventsRequest request, String jobId);
}
