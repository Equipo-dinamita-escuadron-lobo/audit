package com.audit.application.dto.responses;

import java.util.List;

import com.audit.domain.model.AuditSession;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionsPageResponse {
    private List<SessionAuditResponse> sessions;
    private Long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    public static SessionsPageResponse from(List<AuditSession> sessions, Long totalCount, int page, int size){
        List<SessionAuditResponse> responseList = sessions.stream()
            .map(SessionAuditResponse::from)
            .toList();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        return SessionsPageResponse.builder()
            .sessions(responseList)
            .totalElements(totalCount)
            .totalPages(totalPages)
            .currentPage(page)
            .pageSize(size)
            .hasNext(page < totalPages - 1)
            .hasPrevious(page > 0)
            .build();
    }

}
