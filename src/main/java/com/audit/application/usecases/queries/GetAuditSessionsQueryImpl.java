package com.audit.application.usecases.queries;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.response.SessionAuditResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.GetAuditSessionsQuery;

import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.model.CombinedSession;
import com.audit.domain.model.PageResult;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

/**
 * @brief Use case implementation for querying audit sessions with multiple filters
 * Supports pagination and returns structured page response
 */
@Service
public class GetAuditSessionsQueryImpl implements GetAuditSessionsQuery {
    private final AuditSessionRepositoryPort auditSessionRepository;

    public GetAuditSessionsQueryImpl(AuditSessionRepositoryPort auditSessionRepository) {
        this.auditSessionRepository = auditSessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SessionAuditResponse> execute(GetSessionsRequest request) {

        AuditSessionFilter filter = mapToFilter(request);

        PageResult<CombinedSession> pageResult = auditSessionRepository.findCombinedSessions(filter);

        List<SessionAuditResponse> sessions = pageResult.getContent().stream()
            .map(this::toResponse)
            .toList();

        int totalPages = (int) Math.ceil(
            (double) pageResult.getTotalElements() / request.getSize()
        );
        
        return PageResponse.<SessionAuditResponse>builder()
                .data(sessions)
                .totalElements(pageResult.getTotalElements())
                .totalPages(totalPages)
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .hasNext(request.getPage() < totalPages - 1)
                .hasPrevious(request.getPage() > 0)
                .build();
    }

    private SessionAuditResponse toResponse(CombinedSession session) {
        return SessionAuditResponse.builder()
                .userName(session.getUserName())
                .userRole(session.getUserRole().name())
                .loginTime(session.getLoginTime())
                .logoutTime(session.getLogoutTime())
                .build();
    }

    private AuditSessionFilter mapToFilter(GetSessionsRequest request) {
        return AuditSessionFilter.builder()
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .userName(request.getUserName())
                .userRole(request.getUserRole())
                .action(request.getAction())
                .page(request.getPage())
                .size(request.getSize())
                .sortField(request.getSortField())
                .sortDirection(request.getSortDirection())
                .requestingUserRole(request.getRequestingUserRole())
                .build();
    }
    
}
