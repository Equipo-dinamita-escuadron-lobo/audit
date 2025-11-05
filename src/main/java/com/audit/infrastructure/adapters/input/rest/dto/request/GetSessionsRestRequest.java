package com.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class GetSessionsRestRequest {

    @NotNull(message = "Date from is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateTo;

    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    private UserRole userRole;

    private UserAction action;

    @Builder.Default
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 20;

    @Builder.Default
    @Pattern(regexp = "^(actionAt|userName|userRole)$", message = "Invalid sort field")
    private String sortField = "actionAt";

    @Builder.Default
    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort direction must be ASC or DESC")
    private String sortDirection = "DESC";
}
