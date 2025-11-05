package com.audit.application.usecases.queries;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.responses.ExportSessionsResponse;
import com.audit.application.dto.responses.SessionAuditResponse;
import com.audit.application.port.input.queries.ExportAuditSessionsQuery;
import com.audit.application.port.output.FileExportService;
import com.audit.domain.enums.UserAction;
import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.model.PageResult;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

public class ExportAuditSessionsQueryImpl implements ExportAuditSessionsQuery {

    private static final int MAX_EXPORT_RECORDS = 10000;
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
                .userName(request.getUserName())
                .userRole(request.getUserRole())
                .action(request.getAction())
                .sortField(request.getSortField()) 
                .sortDirection(request.getSortDirection())
                .build();

        PageResult<AuditSession> pageResult = auditSessionRepository.findPageByFilters(filter);
        if (pageResult.getTotalElements() > MAX_EXPORT_RECORDS) {
            throw new IllegalArgumentException(
                    "Cannot export more than " + MAX_EXPORT_RECORDS + " records. " +
                            "Current query returns " + pageResult.getTotalElements() + " records.");
        } 
        List<SessionAuditResponse> combinedSessions = combineLoginLogout(pageResult.getContent());

        byte[] fileContent = exportService.exportSessions(combinedSessions, request.getFormat());
        String fileName = generateFileName(request.getFormat());

        if (request.getFormat().equalsIgnoreCase("PDF")) {
            return ExportSessionsResponse.pdf(fileContent, fileName, combinedSessions.size());
        }
        return ExportSessionsResponse.excel(fileContent, fileName, combinedSessions.size());
    }

    private String generateFileName(String format) {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = format.equalsIgnoreCase("PDF") ? ".pdf" : ".xlsx";
        return "audit_sessions_" + timestamp + extension;
    }

    private List<SessionAuditResponse> combineLoginLogout(List<AuditSession> sessions) {
        Map<String, List<AuditSession>> sessionMap = sessions.stream()
                .collect(Collectors.groupingBy(AuditSession::getSessionId));

        List<SessionAuditResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<AuditSession>> entry : sessionMap.entrySet()) {
            List<AuditSession> sessionEvents = entry.getValue();
            AuditSession loginEvent = sessionEvents.stream()
                    .filter(s -> s.getAction() == UserAction.LOGIN)
                    .findFirst()
                    .orElse(null);
            AuditSession logoutEvent = sessionEvents.stream()
                    .filter(s -> s.getAction() == UserAction.LOGOUT)
                    .findFirst()
                    .orElse(null);
            
            if (loginEvent != null) {
                ZonedDateTime logoutTime = (logoutEvent != null) ? logoutEvent.getActionAt() : null;
                SessionAuditResponse response = SessionAuditResponse.builder()
                        .userName(loginEvent.getUserName())
                        .userRole(loginEvent.getUserRole().name())
                        .loginTime(loginEvent.getActionAt())
                        .logoutTime(logoutTime)
                        .build();
                result.add(response);
            }
        }
        return result;
    }

}
