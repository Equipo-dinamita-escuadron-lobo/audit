package com.audit.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.PageResponse;
//port com.audit.application.port.input.queries.ExportAuditOperationsQuery;
import com.audit.application.port.input.queries.GetAuditOperationsQuery;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.OperationRestMapper;
//port com.audit.infrastructure.adapters.output.security.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;



@RestController
@RequestMapping("/api/audit/operations")
@RequiredArgsConstructor
@Slf4j
public class AuditOperationController {

    private final GetAuditOperationsQuery getAuditOperationsQuery;
    private final OperationRestMapper operationRestMapper;

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','PROFESOR','ESTUDIANTE')")
    public ResponseEntity<PageResponse<OperationAuditResponse>> getAuditOperations(
        @Valid @ModelAttribute GetOperationsRestRequest restRequest, 
            HttpServletRequest httpServletRequest) {
        GetOperationsRequest request =
                operationRestMapper.toGetOperationsRequest(restRequest, httpServletRequest);
        PageResponse<OperationAuditResponse> response = getAuditOperationsQuery.execute(request);
        return ResponseEntity.ok(response);
    }
    
}
