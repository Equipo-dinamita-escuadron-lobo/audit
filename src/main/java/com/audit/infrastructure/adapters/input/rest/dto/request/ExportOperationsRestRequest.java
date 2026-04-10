package com.audit.infrastructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import com.audit.domain.enums.OperationType;
import com.audit.domain.enums.UserRole;
import com.audit.infrastructure.adapters.input.rest.validation.ValidateRange;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateRange
public class ExportOperationsRestRequest implements DateRangeRequest{

    @NotNull(message = "Date from is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateTo;

    @Size(max = 100, message = "Module name must not exceed 100 characters")
    private String moduleName;

    @Size(max = 100, message = "Table name must not exceed 100 characters")
    private String affectedTable;

    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    private UserRole userRole;

    private OperationType operationType;

    @Size(max = 100, message = "Register ID must not exceed 100 characters")
    private String registerId;

    @Builder.Default
    private String sortField = "operationAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
