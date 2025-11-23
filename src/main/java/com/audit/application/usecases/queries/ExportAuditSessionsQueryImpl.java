package com.audit.application.usecases.queries;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.response.ExportFileResponse;
import com.audit.application.dto.response.SessionAuditResponse;
import com.audit.application.port.input.queries.ExportAuditSessionsQuery;
import com.audit.application.port.output.FileExportService;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.model.CombinedSession;
import com.audit.domain.model.PageResult;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

@Service
public class ExportAuditSessionsQueryImpl implements ExportAuditSessionsQuery {

    private static final int MAX_EXPORT_RECORDS = 10000;
    private final AuditSessionRepositoryPort auditSessionRepository;
    private final FileExportService exportService;

    public ExportAuditSessionsQueryImpl(AuditSessionRepositoryPort auditSessionRepository, FileExportService exportService) {
        this.auditSessionRepository = auditSessionRepository;
        this.exportService = exportService;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportFileResponse execute(ExportSessionsRequest request) {

        AuditSessionFilter filter = AuditSessionFilter.builder()
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .userName(request.getUserName())
                .userRole(request.getUserRole())
                .action(request.getAction())
                .sortField(request.getSortField()) 
                .sortDirection(request.getSortDirection())
                .page(0)  // Para export siempre página 0
                .size(MAX_EXPORT_RECORDS)  // Limitar al máximo de registros
                .requestingUserRole(request.getRequestedBy())
                .build();

        // Usar findCombinedSessions en lugar de findPageByFilters + combineLoginLogout
        // Esto mueve la lógica de combinación a SQL, mucho más eficiente
        PageResult<CombinedSession> pageResult = auditSessionRepository.findCombinedSessions(filter);
        
        if (pageResult.getTotalElements() > MAX_EXPORT_RECORDS) {
            throw new IllegalArgumentException(
                    "Cannot export more than " + MAX_EXPORT_RECORDS + " records. " +
                            "Current query returns " + pageResult.getTotalElements() + " records.");
        }
        
        // Convertir directamente de CombinedSession a SessionAuditResponse
        List<SessionAuditResponse> sessions = pageResult.getContent().stream()
                .map(this::toResponse)
                .toList();

        byte[] fileContent = exportService.exportSessions(sessions, request.getFormat());
        String fileName = generateFileName(request.getFormat());

        if (request.getFormat().equalsIgnoreCase("PDF")) {
            return ExportFileResponse.pdf(fileContent, fileName, sessions.size());
        }
        return ExportFileResponse.excel(fileContent, fileName, sessions.size());
    }

    private String generateFileName(String format) {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = format.equalsIgnoreCase("PDF") ? ".pdf" : ".xlsx";
        return "audit_sessions_" + timestamp + extension;
    }

    private SessionAuditResponse toResponse(CombinedSession session) {
        return SessionAuditResponse.builder()
                .userName(session.getUserName())
                .userRole(session.getUserRole().name())
                .loginTime(session.getLoginTime())
                .logoutTime(session.getLogoutTime())
                .build();
    }

}
