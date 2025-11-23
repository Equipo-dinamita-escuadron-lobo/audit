package com.audit.application.dto.request;


import java.time.ZonedDateTime;

import com.audit.domain.enums.OperationType;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExportOperationsRequest {

    private ZonedDateTime dateFrom;
    private ZonedDateTime dateTo;

    private String moduleName;
    private String affectedTable;
    private String userName;
    private UserRole userRole;
    private OperationType operationType;
    private String registerId;
    private String enterpriseId;

    private String sortField;
    private String sortDirection;

    private String requestingUserRole;
}
