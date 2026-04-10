package com.audit.application.dto.request;

import java.time.Instant;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;

/**
 * @brief DTO for exporting session events
 */
@Getter
@Builder
public class ExportSessionsRequest {
    
    private String requestedBy;
    private Instant dateFrom;
    private Instant dateTo;
    private String userName;
    private UserRole userRole;
    private UserAction action;

    @Builder.Default
    private String format = "EXCEL";

    @Builder.Default
    private String sortField = "actionAt";

    @Builder.Default
    private String sortDirection = "DESC";

}
