package com.audit.application.dto.response;

import java.time.Instant;

import com.audit.domain.model.OperationData;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OperationAuditResponse {

    private String userName;
    private String userRole;
    private Instant operationAt;
    private String moduleName;
    private String affectedTable;
    private String registerId;
    private String operationType;
    private OperationData dataObject;
}
