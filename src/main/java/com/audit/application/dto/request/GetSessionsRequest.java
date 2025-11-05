package com.audit.application.dto.request;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @brief DTO for session event queries with multiple filters
 */
@Getter
@Setter
@Builder
public class GetSessionsRequest {

    private ZonedDateTime dateFrom;
    private ZonedDateTime dateTo;
    private String userName;
    private UserRole userRole;
    private UserAction action;
    private String requestingUserRole;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortField = "actionAt";

    @Builder.Default
    private String sortDirection = "DESC";

}
