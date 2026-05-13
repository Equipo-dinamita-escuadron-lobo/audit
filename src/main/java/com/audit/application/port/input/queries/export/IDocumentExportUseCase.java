package com.audit.application.port.input.queries.export;

import com.audit.application.dto.request.ExportDocumentsEventsRequest;

public interface IDocumentExportUseCase {

    String execute(ExportDocumentsEventsRequest request);
}
