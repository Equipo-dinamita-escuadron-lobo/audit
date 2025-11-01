package com.audit.infrastructure.adapters.input.rest.mapper;

import org.springframework.stereotype.Component;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetSessionsRestRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionRestMapper {


    public GetSessionsRequest toGetSessionsRequest(GetSessionsRestRequest restRequest) {
        GetSessionsRequest.GetSessionsRequestBuilder builder = GetSessionsRequest.builder()
            .dateFrom(restRequest.getDateFrom())
            .dateTo(restRequest.getDateTo())
            .userId(restRequest.getUserId())
            .userName(restRequest.getUserName())
            .userRole(restRequest.getUserRole())
            .action(restRequest.getAction())
            .ipAddress(restRequest.getIpAddress())
            .page(restRequest.getPage())
            .size(restRequest.getSize())
            .sortField(restRequest.getSortField())
            .sortDirection(restRequest.getSortDirection());
            

        return builder.build();
                
    }

    public ExportSessionsRequest toExportSessionsRequest(ExportSessionsRestRequest restRequest) {
        return ExportSessionsRequest.builder()
                .dateFrom(restRequest.getDateFrom())
                .dateTo(restRequest.getDateTo())
                .userId(restRequest.getUserId())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .action(restRequest.getAction())
                .ipAddress(restRequest.getIpAddress())
                .format(restRequest.getFormat())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                .build();
    }
}
