package com.audit.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.application.dto.response.ExportFileResponse;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.ExportAuditOperationsQuery;
import com.audit.application.port.input.queries.GetAuditOperationsQuery;
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
    private final OperationRestMapper operationRestMapper;
    private final ExportAuditOperationsQuery exportAuditOperationsQuery;

    @GetMapping()
    @PreAuthorize("hasAnyRole('Administrador','Profesor','Estudiante')")
    public ResponseEntity<PageResponse<OperationAuditResponse>> getAuditOperations(
        @Valid @ModelAttribute GetOperationsRestRequest restRequest, 
            HttpServletRequest httpServletRequest) {
        GetOperationsRequest request =
                operationRestMapper.toGetOperationsRequest(restRequest, httpServletRequest);
        PageResponse<OperationAuditResponse> response = getAuditOperationsQuery.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export_excel")
    @PreAuthorize("hasRole('Administrador')")
    //@PreAuthorize("hasAuthority('Export_Excel_Audit_Operations')")
    public ResponseEntity<byte[]> exportOperationsExcel(
            @Valid @RequestBody ExportOperationsRestRequest restRequest,
            HttpServletRequest httpServletRequest) {
        ExportOperationsRequest request = operationRestMapper.toExportOperationsRequest(restRequest,
                httpServletRequest);
        ExportFileResponse response = exportAuditOperationsQuery.execute(request);
        return buildFileResponse(response);
    }

    private ResponseEntity<byte[]> buildFileResponse(ExportFileResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(response.getContentType()));
        headers.setContentDispositionFormData("attachment", response.getFileName());
        headers.setContentLength(response.getFileSize());
        return new ResponseEntity<>(response.getFileContent(), headers, HttpStatus.OK);
    }
    
}
