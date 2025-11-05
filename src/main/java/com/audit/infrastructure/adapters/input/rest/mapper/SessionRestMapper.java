package com.audit.infrastructure.adapters.input.rest.mapper;

import org.springframework.stereotype.Component;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetSessionsRestRequest;
import com.audit.infrastructure.adapters.output.security.SecurityContextService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionRestMapper {

    private final SecurityContextService securityContextService;

    public GetSessionsRequest toGetSessionsRequest(GetSessionsRestRequest restRequest) {

        String requestingUserRole = securityContextService.getCurrentUserRole();

        return GetSessionsRequest.builder()
                .dateFrom(restRequest.getDateFrom())
                .dateTo(restRequest.getDateTo())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .action(restRequest.getAction())
                .page(restRequest.getPage())
                .size(restRequest.getSize())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                .requestingUserRole(requestingUserRole)
                .build();
    }

    public ExportSessionsRequest toExportSessionsRequest(ExportSessionsRestRequest restRequest) {
        //String requestingUserRole = securityContextService.getCurrentUserRole();
        return ExportSessionsRequest.builder()
                .dateFrom(restRequest.getDateFrom())
                .dateTo(restRequest.getDateTo())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .action(restRequest.getAction())
                .format(restRequest.getFormat())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                //.requestingUserRole(requestingUserRole)
                .build();
    }
}
