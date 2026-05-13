package com.audit.application.port.input.queries.export;

import com.audit.application.dto.request.ExportSessionsRequest;

public interface ISessionExportUseCase {

    String execute(ExportSessionsRequest request);
}
