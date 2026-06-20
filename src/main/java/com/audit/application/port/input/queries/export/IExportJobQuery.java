package com.audit.application.port.input.queries.export;

import java.util.Optional;

import com.audit.application.dto.response.ExportFileResult;
import com.audit.application.dto.response.ExportJobResponse;

public interface IExportJobQuery {

    Optional<ExportJobResponse> getJobStatus(String jobId);

    ExportFileResult getJobFileData(String jobId);

    void removeJob(String jobId);
}
