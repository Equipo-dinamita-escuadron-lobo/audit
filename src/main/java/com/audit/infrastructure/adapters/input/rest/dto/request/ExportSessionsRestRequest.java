package com.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportSessionsRestRequest {

    @NotNull(message = "Date from is required")
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    private ZonedDateTime dateTo;

    private String userId;
    private String userName;
    private UserRole userRole;
    private UserAction action;
    private String ipAddress;

    @Builder.Default
    @NotBlank(message = "Format is required")
    @Pattern(regexp = "^(EXCEL|PDF)$", message = "Format must be EXCEL or PDF")
    private String format = "EXCEL";

    @Builder.Default
    private String sortField = "actionAt";
    @Builder.Default
    private String sortDirection = "DESC";

}
