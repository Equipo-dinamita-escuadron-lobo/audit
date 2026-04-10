package com.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;
import com.audit.infrastructure.adapters.input.rest.validation.ValidateRange;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateRange
public class ExportSessionsRestRequest implements DateRangeRequest {

    @NotNull(message = "Date from is required")
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    private ZonedDateTime dateTo;
    
    private String userName;
    private UserRole userRole;
    private UserAction action;

    @Builder.Default
    private String sortField = "loginTime";
    @Builder.Default
    private String sortDirection = "DESC";

}
