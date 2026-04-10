package com.audit.application.usecases.queries;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.response.ExportFileResponse;
import com.audit.application.dto.response.SessionAuditResponse;
import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.ExportConstraints;
import com.audit.application.port.input.queries.ExportAuditSessionsQuery;
import com.audit.application.port.output.FileExportService;
import com.audit.domain.model.AuditSessionCriteria;
import com.audit.domain.port.output.AuditSessionRepositoryPort;



@Service
public class ExportAuditSessionsQueryImpl implements ExportAuditSessionsQuery {
    
    private final AuditSessionRepositoryPort auditSessionRepository;
    private final FileExportService exportService;

    public ExportAuditSessionsQueryImpl(AuditSessionRepositoryPort auditSessionRepository,
            FileExportService exportService) {
        this.auditSessionRepository = auditSessionRepository;
        this.exportService = exportService;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportFileResponse execute(ExportSessionsRequest request) {

        AuditSessionCriteria criteria = AuditSessionCriteria.create(
                request.getDateFrom(),
                request.getDateTo(),
                request.getUserName(),
                request.getUserRole(),
                request.getAction());

        String format = request.getFormat().toUpperCase();
        long totalRecords = auditSessionRepository.countByCriteria(criteria);

        ExportConstraints.validateExportable(totalRecords, format);

        List<CombinedSession> sessions = auditSessionRepository.findForExport(criteria);

        List<SessionAuditResponse> data = sessions.stream()
                .map(this::toResponse)
                .toList();

        byte[] fileContent = exportService.exportSessions(data, format);

        return buildResponse(fileContent, format, data.size());
    }

    private SessionAuditResponse toResponse(CombinedSession session) {
        return SessionAuditResponse.builder()
                .userName(session.getUserName())
                .userRole(session.getUserRole().name())
                .loginTime(session.getLoginTime())
                .logoutTime(session.getLogoutTime())
                .build();
    }

    private ExportFileResponse buildResponse(byte[] content, String format, int recordCount) {
        String timestamp = Instant.now()
                .atZone(ZoneId.of("America/Bogota"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = "PDF".equals(format) ? ".pdf" : ".xlsx";
        String fileName = "audit_sessions_" + timestamp + extension;

        return "PDF".equals(format)
                ? ExportFileResponse.pdf(content, fileName, recordCount)
                : ExportFileResponse.excel(content, fileName, recordCount);
    }

}
