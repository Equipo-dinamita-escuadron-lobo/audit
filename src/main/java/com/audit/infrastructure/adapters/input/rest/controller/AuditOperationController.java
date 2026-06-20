package com.audit.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.request.GetModulesTablesRequest;
import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.application.dto.response.ModuleTableResponse;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.GetAuditOperationsQuery;
import com.audit.application.port.input.queries.GetModulesAndTablesQuery;
import com.audit.application.port.input.queries.export.IOperationExportUseCase;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.OperationRestMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/audit/operations")
@RequiredArgsConstructor
@Slf4j
public class AuditOperationController {

    private final GetAuditOperationsQuery getAuditOperationsQuery;
    private final GetModulesAndTablesQuery getModulesAndTablesQuery;
    private final OperationRestMapper operationRestMapper;
    private final IOperationExportUseCase operationExportUseCase;

    @GetMapping()
    @PreAuthorize("hasAnyRole('Administrador','Profesor','Estudiante')")
    public ResponseEntity<PageResponse<OperationAuditResponse>> getAuditOperations(
            @Valid @ModelAttribute GetOperationsRestRequest restRequest,
            HttpServletRequest httpServletRequest) {
        GetOperationsRequest request = operationRestMapper.toGetOperationsRequest(restRequest, httpServletRequest);
        PageResponse<OperationAuditResponse> response = getAuditOperationsQuery.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/modules-tables")
    @PreAuthorize("hasAnyRole('Administrador','Profesor','Estudiante')")
    public ResponseEntity<List<ModuleTableResponse>> getModulesAndTables(
            HttpServletRequest httpServletRequest) {
        GetModulesTablesRequest request = operationRestMapper.toGetModulesTablesRequest(httpServletRequest);
        return ResponseEntity.ok(getModulesAndTablesQuery.execute(request));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<Map<String, String>> exportOperations(
            @Valid @RequestBody ExportOperationsRestRequest restRequest,
            HttpServletRequest httpServletRequest) {
        ExportOperationsRequest request = operationRestMapper
                .toExportOperationsRequest(restRequest, httpServletRequest);
        String jobId = operationExportUseCase.execute(request);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

}
