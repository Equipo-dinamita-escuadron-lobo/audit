package com.audit.domain.model;

import java.time.ZonedDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AuditSessionFilter that = (AuditSessionFilter) o;
        return Objects.equals(dateFrom, that.dateFrom) &&
                Objects.equals(dateTo, that.dateTo) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(userRole, that.userRole) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateFrom, dateTo, userName, userRole, action);
    }
}
