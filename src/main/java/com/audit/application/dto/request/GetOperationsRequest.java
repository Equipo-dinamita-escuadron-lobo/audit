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
public class GetOperationsRequest {

    private ZonedDateTime dateFrom;
    private ZonedDateTime dateTo;
    private String moduleName;
    private String affectedTable;
    private String userName;
    private UserRole userRole;
    private OperationType operationType;
    private String registerId;
    private String enterpriseId;

    private String requestingUserRole;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortField = "operationAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
