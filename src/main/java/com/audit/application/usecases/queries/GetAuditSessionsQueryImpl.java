package com.audit.application.usecases.queries;

import java.util.List;

import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.responses.SessionsPageResponse;
import com.audit.application.port.input.queries.GetAuditSessionsQuery;
import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

/**
 * @brief Use case implementation for querying audit sessions with multiple filters
 * Supports pagination and returns structured page response
 */

public class GetAuditSessionsQueryImpl implements GetAuditSessionsQuery {
    private final AuditSessionRepositoryPort auditSessionRepository;

    public GetAuditSessionsQueryImpl(AuditSessionRepositoryPort auditSessionRepository) {
        this.auditSessionRepository = auditSessionRepository;
    }

    @Override
    public SessionsPageResponse execute(GetSessionsRequest request) {

        AuditSessionFilter filter = mapToFilter(request);

        List<AuditSession> sessions = auditSessionRepository.findByFilters(filter);
        long totalCount = auditSessionRepository.countByFilters(filter);

        return SessionsPageResponse.from(
                sessions,
                totalCount,
                request.getPage(),
                request.getSize());
    }
    
    private AuditSessionFilter mapToFilter(GetSessionsRequest request) {
        return AuditSessionFilter.builder()
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .action(request.getAction())
                .page(request.getPage())
                .size(request.getSize())
                .userRole(request.getUserRole())
                .build();
    }
    
}
