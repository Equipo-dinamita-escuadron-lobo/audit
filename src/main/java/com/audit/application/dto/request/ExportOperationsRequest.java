package com.audit.application.dto.request;


import java.time.Instant;

import com.audit.domain.enums.OperationType;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportOperationsRequest {

    private Instant dateFrom;
    private Instant dateTo;

    private String moduleName;
    private String affectedTable;
    private String userName;
    private UserRole userRole;
    private OperationType operationType;
    private String registerId;
    private String enterpriseId;

    @Builder.Default
    private String format = "EXCEL";

    private String sortField;
    private String sortDirection;

    private String requestingUserRole;
}
