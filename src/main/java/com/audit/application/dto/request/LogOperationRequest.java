package com.audit.application.dto.request;

import java.time.ZonedDateTime;

import com.audit.domain.enums.OperationType;
import com.audit.domain.enums.UserRole;
import com.audit.domain.model.OperationData;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LogOperationRequest {

    private String enterpriseId;
    private String userId;
    private String userName;
    private UserRole userRole;
    private OperationType operationType;
    private ZonedDateTime operationAt;
    private String moduleName; 
    private String affectedTable;
    private String registerId;
    private OperationData dataObject;
}
