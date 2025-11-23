package com.audit.infrastructure.adapters.input.rest.mapper;

import org.springframework.stereotype.Component;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetOperationsRestRequest;
import com.audit.infrastructure.adapters.output.exception.security.MisingHeaderException;
import com.audit.infrastructure.adapters.output.security.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationRestMapper {

    private final SecurityContextService securityContextService;

    public GetOperationsRequest toGetOperationsRequest(
            GetOperationsRestRequest restRequest,
            HttpServletRequest httpServletRequest) {
        //Estos dos datos se extraen de la cabecera que es enviada desde el front 
        //Estrae el enterpriseId del header para filtrar por empresa
        String enterpriseId = httpServletRequest.getHeader("X-Enterprise-Id");
        if (enterpriseId == null || enterpriseId.isBlank()) {
            throw new MisingHeaderException("X-Enterprise-Id header is required");
        }
        //Estrae el rol del usuario para saber si puede consultar esa informacion
        String requestingUserRole = securityContextService.getCurrentUserRole();
        return GetOperationsRequest.builder()
                .dateFrom(restRequest.getDateFrom())
                .dateTo(restRequest.getDateTo())
                .moduleName(restRequest.getModuleName())
                .affectedTable(restRequest.getAffectedTable())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .operationType(restRequest.getOperationType())
                .registerId(restRequest.getRegisterId())
                .enterpriseId(enterpriseId)
                .page(restRequest.getPage())
                .size(restRequest.getSize())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                .requestingUserRole(requestingUserRole)
                .build();
    }

    public ExportOperationsRequest toExportOperationsRequest(
            ExportOperationsRestRequest restRequest,
            HttpServletRequest httpRequest) {

        String enterpriseId = httpRequest.getHeader("X-Enterprise-Id");
        if (enterpriseId == null || enterpriseId.isBlank()) {
            throw new MisingHeaderException("X-Enterprise-Id header is required");
        }
        String requestingUserRole = securityContextService.getCurrentUserRole();

        return ExportOperationsRequest.builder()
                .enterpriseId(enterpriseId)
                .dateFrom(restRequest.getDateFrom())
                .dateTo(restRequest.getDateTo())
                .moduleName(restRequest.getModuleName())
                .affectedTable(restRequest.getAffectedTable())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .operationType(restRequest.getOperationType())
                .registerId(restRequest.getRegisterId())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                .requestingUserRole(requestingUserRole)
                .build();
    }
}
