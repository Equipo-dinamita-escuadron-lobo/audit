package com.audit.domain.model;

import java.time.ZonedDateTime;

import com.audit.domain.enums.OperationType;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AuditOperationFilter {

    private final ZonedDateTime dateFrom;
    private final ZonedDateTime dateTo;
    
    private final String moduleName;
    private final String affectedTable;
    private final String userName;
    private final UserRole userRole;
    private final OperationType operationType;
    private final String registerId;
    private final String enterpriseId;

    private final Integer page;
    private final Integer size;
    private final String sortField;
    private final String sortDirection;

    private final String requestingUserRole;

    public boolean hasDateRange() {
        return dateFrom != null && dateTo != null;
    }

    public boolean hasModuleNameFilter() {
        return moduleName != null && !moduleName.isBlank();
    }

    public boolean hasAffectedTableFilter() {
        return affectedTable != null && !affectedTable.isBlank();
    }

    public boolean hasUserNameFilter() {
        return userName != null && !userName.isBlank();
    }

    public boolean hasUserRoleFilter() {
        return userRole != null;
    }

    public boolean hasOperationTypeFilter() {
        return operationType != null;
    }

    public boolean hasRegisterIdFilter() {
        return registerId != null && !registerId.isBlank();
    }

    public boolean hasEnterpriseIdFilter() {
        return enterpriseId != null && !enterpriseId.isBlank();
    }

    public boolean hasPagination() {
        return page != null && size != null;
    }

    public boolean hasSorting() {
        return sortField != null && sortDirection != null;
    }
}
