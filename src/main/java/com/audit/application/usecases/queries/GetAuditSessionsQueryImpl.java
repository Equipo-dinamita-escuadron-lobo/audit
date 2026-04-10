package com.audit.application.usecases.queries;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.response.SessionAuditResponse;
//import com.audit.application.internal.AccessValidator;
import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.PageResult;
import com.audit.application.internal.QueryOptions;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.GetAuditSessionsQuery;
import com.audit.domain.model.AuditSessionCriteria;
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

        //AccessValidator.validateSessionAccess(request.getRequestingUserRole());

        AuditSessionCriteria criteria = AuditSessionCriteria.create(
                request.getDateFrom(),
                request.getDateTo(),
                request.getUserName(),
                request.getUserRole(),
                request.getAction());

        QueryOptions options = QueryOptions.builder()
                .page(request.getPage())
                .size(request.getSize())
                .sortField(request.getSortField())
                .sortDirection(request.getSortDirection())
                .build();

        PageResult<CombinedSession> pageResult = auditSessionRepository.findCombinedSessions(criteria, options);

        List<SessionAuditResponse> data = pageResult.getContent().stream()
                .map(this::toResponse)
                .toList();

        return buildPageResponse(data, pageResult.getTotalElements(), request.getPage(), request.getSize());
    }

    private SessionAuditResponse toResponse(CombinedSession session) {
        return SessionAuditResponse.builder()
                .userName(session.getUserName())
                .userRole(session.getUserRole().name())
                .loginTime(session.getLoginTime())
                .logoutTime(session.getLogoutTime())
                .build();
    }

    private PageResponse<SessionAuditResponse> buildPageResponse(
            List<SessionAuditResponse> data, long totalElements, int page, int size) {
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return PageResponse.<SessionAuditResponse>builder()
                .data(data)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
    
}
