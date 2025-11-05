package com.audit.application.dto.request;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @brief DTO for exporting session events
 */
@Getter
@Setter
@Builder
public class ExportSessionsRequest {
    
    private String requestedBy;
    private ZonedDateTime dateFrom;
    private ZonedDateTime dateTo;
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
