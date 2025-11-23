package com.audit.domain.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.audit.domain.enums.OperationType;
import com.audit.domain.enums.UserRole;
import com.audit.domain.exceptions.InvalidAuditEventException;

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
    private final String moduleName;
    private final String affectedTable;
    private final String registerId;
    private final String enterpriseId;
    private final OperationData dataObject;
    private final ZonedDateTime createdAt;

    // Constructor
    private AuditOperation(Long id, String userId, String userName, UserRole userRole, OperationType operationType,
            ZonedDateTime operationAt, String moduleName, String affectedTable, String registerId, String enterpriseId,
            OperationData dataObject,
            ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.operationType = operationType;
        this.operationAt = operationAt;
        this.moduleName = moduleName;
        this.affectedTable = affectedTable;
        this.registerId = registerId;
        this.enterpriseId = enterpriseId;
        this.dataObject = dataObject;
        this.createdAt = createdAt;
    }

    // Factory method
    public static AuditOperation create(String userId, String userName, UserRole userRole,
            OperationType operationType, ZonedDateTime operationAt, String moduleName, String affectedTable,
            String registerId, String enterpriseId,OperationData dataObject){
        validateEnterpriseId(enterpriseId);
        validateUserData(userId, userName, userRole);
        validateOperationData(operationType, affectedTable, registerId, dataObject);
        validateModuleName(moduleName);
        validateOperationAt(operationAt);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        validateTimestamp(now);

        return new AuditOperation(null, userId, userName, userRole, operationType, operationAt, moduleName,
                affectedTable, registerId, enterpriseId, dataObject, now);
    }

    public static AuditOperation reconstruct(Long id, String userId, String userName, UserRole userRole,
            OperationType operationType, ZonedDateTime operationAt, String moduleName, String affectedTable, String registerId, String enterpriseId,
            OperationData dataObject, ZonedDateTime createdAt) {
        return new AuditOperation(id, userId, userName, userRole, operationType,
                operationAt, moduleName, affectedTable, registerId, enterpriseId, dataObject, createdAt);
    }

    private static void validateEnterpriseId(String enterpriseId) {
        if (enterpriseId == null || enterpriseId.isBlank()) {
            throw new InvalidAuditEventException("Enterprise ID cannot be null or empty");
        }
    }

    private static void validateModuleName(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            throw new InvalidAuditEventException("Module name cannot be null or empty");
        }
    }
    
    private static void validateUserData(String userId, String userName, UserRole userRole) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidAuditEventException("User ID cannot be null or empty");
        }
        if (userName == null || userName.isBlank()) {
            throw new InvalidAuditEventException("User name cannot be null or empty");
        }
        if (userRole == null) {
            throw new InvalidAuditEventException("User role cannot be null");
        }
    }

    private static void validateOperationData(OperationType operationType, String affectedTable,
            String registerId, OperationData dataObject) {
        if (operationType == null) {
            throw new InvalidAuditEventException("Operation type cannot be null");
        }
        if (affectedTable == null || affectedTable.isBlank()) {
            throw new InvalidAuditEventException("Affected table cannot be null or empty");
        }
        if (registerId == null || registerId.isBlank()) {
            throw new InvalidAuditEventException("Register ID cannot be null or empty");
        }
        if (dataObject == null) {
            throw new InvalidAuditEventException("Data object cannot be null");
        }
        switch (operationType) {
            case CREATE:
            case DELETE:
            case INACTIVATE:
                if (dataObject.getEntity().isEmpty()) {
                    throw new InvalidAuditEventException(operationType + " requires entity data");
                }
                if (!dataObject.getChanges().isEmpty()) {
                    throw new InvalidAuditEventException(operationType + " cannot contain changes");
                }
                break;

            case UPDATE:
                if (dataObject.getChanges().isEmpty()) {
                    throw new InvalidAuditEventException("UPDATE requires change data");
                }
                if (!dataObject.getEntity().isEmpty()) {
                    throw new InvalidAuditEventException("UPDATE cannot contain entity data");
                }
                break;

            default:
                throw new InvalidAuditEventException("Unsupported operation type: " + operationType);
        }
    }

    private static void validateOperationAt(ZonedDateTime operationAt) {
        if (operationAt == null) {
            throw new InvalidAuditEventException("Operation timestamp cannot be null");
        }
        if (operationAt.isAfter(ZonedDateTime.now().plusMinutes(5))) {
            throw new InvalidAuditEventException("Operation timestamp cannot be in the future");
        }
    }

    private static void validateTimestamp(ZonedDateTime timestamp) {
        if (timestamp == null) {
            throw new InvalidAuditEventException("null");
        }
        if (timestamp.isAfter(ZonedDateTime.now().plusMinutes(5))) {
            throw new InvalidAuditEventException(timestamp.toString());
        }
    }
}
