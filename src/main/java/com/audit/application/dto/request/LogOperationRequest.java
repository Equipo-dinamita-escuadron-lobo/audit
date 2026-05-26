package com.audit.application.dto.request;

import java.time.Instant;
import java.util.List;

import com.audit.domain.enums.OperationType;
import com.audit.domain.model.OperationData;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogOperationRequest {

    private String enterpriseId;
    private String userId;
    private String userName;
    private List<String> userRole;
    private OperationType operationType;
    private Instant operationAt;
    private String moduleName;
    private String affectedTable;
    private String registerId;
    private OperationData dataObject;
}
