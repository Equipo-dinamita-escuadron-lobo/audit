package com.audit.domain.model;

import java.time.ZonedDateTime;

import com.audit.domain.enums.OperationType;
import com.audit.domain.enums.UserRole;

import lombok.Getter;

@Getter
public class AuditOperation {

    // Attributes
    private final Long id;
    private final String userId;
    private final String userName;
    private final UserRole userRole;
    private final OperationType operationType;
    private final ZonedDateTime operationAt;
    private final String affectedTable;
    private final Long registerId;
    private final Long enterpriseId;
    private final String dataObject;
    private final ZonedDateTime createdAt;

    // Constructor
    private AuditOperation(Long id, String userId, String userName, UserRole userRole, OperationType operationType,
            ZonedDateTime operationAt, String affectedTable, Long registerId, Long enterpriseId, String dataObject,
            ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.operationType = operationType;
        this.operationAt = operationAt;
        this.affectedTable = affectedTable;
        this.registerId = registerId;
        this.enterpriseId = enterpriseId;
        this.dataObject = dataObject;
        this.createdAt = createdAt;
    }

    // Factory method
    public static AuditOperation create(String userId, String userName, UserRole userRole, OperationType operationType,
            ZonedDateTime operationAt, String affectedTable, Long registerId, Long enterpriseId, String dataObject) {

        ZonedDateTime createdAt = ZonedDateTime.now();

        return new AuditOperation(null, userId, userName, userRole, operationType, operationAt, affectedTable,
                registerId, enterpriseId, dataObject, createdAt);
    }
}
