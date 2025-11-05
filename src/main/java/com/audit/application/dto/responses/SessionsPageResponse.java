package com.audit.application.dto.responses;

import java.util.List;

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

}
