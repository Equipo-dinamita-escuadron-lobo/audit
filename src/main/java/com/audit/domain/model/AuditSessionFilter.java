package com.audit.domain.model;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;

/**
 * @brief Value Object for Session Audit Query Filters
 */
@Getter
@Builder
public class AuditSessionFilter {

    private final ZonedDateTime dateFrom;
    private final ZonedDateTime dateTo;

    private final String userName;
    private final UserRole userRole;
    private final UserAction action;

    private final Integer page;
    private final Integer size;
    private final String sortField;
    private final String sortDirection;

    private final String requestingUserRole;

    public boolean hasDateRange() {
        return dateFrom != null && dateTo != null;
    }

    public boolean hasRoleFilter() {
        return userRole != null;
    }

    public boolean hasUserNameFilter() {
        return userName != null && !userName.isBlank();
    }
    
    public boolean hasActionFilter() {
        return action != null;
    }

    public boolean hasPagination() {
        return page != null && size != null;
    }

    public boolean hasSorting() {
        return sortField != null && sortDirection != null;
    }
}
