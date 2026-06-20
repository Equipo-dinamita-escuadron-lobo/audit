package com.audit.application.dto.request;

import java.time.Instant;

import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.UserAction;

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
    private String userRole;
    private UserAction action;
    private ExportFormat exportFormat;
}
