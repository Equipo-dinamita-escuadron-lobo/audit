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
    private final String userId;
    private final String userName;
    private final UserRole userRole;
    private final String ipAddress;
    private final UserAction action;

    private final Integer page;
    private final Integer size;
    private final String sortField;
    private final String sortDirection;

    public boolean hasDateRange() {
        return dateFrom != null && dateTo != null;
    }

    public boolean hasUserFilter() {
        return userId != null || userName != null;
    }

    public boolean hasRoleFilter() {
        return userRole != null;
    }

    public boolean hasActionFilter() {
        return action != null;
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
                Objects.equals(userId, that.userId) &&
                Objects.equals(userRole, that.userRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateFrom, dateTo, userId, userRole);
    }
}
