package com.audit.application.internal;

import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueryOptions {
    private final int page;
    private final int size;
    private final String sortField;
    private final String sortDirection;
    private final UserRole requestingUserRole;
}
