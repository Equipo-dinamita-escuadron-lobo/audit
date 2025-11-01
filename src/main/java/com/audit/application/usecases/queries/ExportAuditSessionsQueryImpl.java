package com.audit.application.usecases.queries;

import java.util.List;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.responses.ExportSessionsResponse;
import com.audit.application.dto.responses.SessionAuditResponse;
import com.audit.application.port.input.queries.ExportAuditSessionsQuery;
import com.audit.application.port.output.FileExportService;
import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

public class ExportAuditSessionsQueryImpl implements ExportAuditSessionsQuery {

    private final AuditSessionRepositoryPort auditSessionRepository;
    private final FileExportService exportService;

    public ExportAuditSessionsQueryImpl(AuditSessionRepositoryPort auditSessionRepository, FileExportService exportService) {
        this.auditSessionRepository = auditSessionRepository;
        this.exportService = exportService;
    }

    @Override
    public ExportSessionsResponse execute(ExportSessionsRequest request) {
        AuditSessionFilter filter = AuditSessionFilter.builder()
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .userId(request.getUserId())
                .userRole(request.getUserRole())
                .build();
        List<AuditSession> sessions = auditSessionRepository.findByFilters(filter);
        int total = sessions.size();
        String format = request.getFormat();

        List<SessionAuditResponse> sessionDTO = sessions.stream()
            .map(SessionAuditResponse::from)
                .toList();

        byte[] fileContent = exportService.exportSessions(sessionDTO, format);
        String fileName = "sessions_" + System.currentTimeMillis() + 
                (format.equals("PDF") ? ".pdf" : ".xlsx");

        if (format.equalsIgnoreCase("PDF")) {
            return ExportSessionsResponse.pdf(fileContent, fileName, total);
        }
        return ExportSessionsResponse.excel(fileContent, fileName, total);
    }


}
