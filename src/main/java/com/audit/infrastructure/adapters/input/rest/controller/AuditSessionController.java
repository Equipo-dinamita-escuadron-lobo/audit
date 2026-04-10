package com.audit.infrastructure.adapters.input.rest.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.response.ExportFileResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.dto.response.SessionAuditResponse;
import com.audit.application.port.input.queries.ExportAuditSessionsQuery;
import com.audit.application.port.input.queries.GetAuditSessionsQuery;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.SessionRestMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/audit/sessions")
@RequiredArgsConstructor
@Slf4j
public class AuditSessionController {

    private final GetAuditSessionsQuery getAuditSessionsQuery;
    private final ExportAuditSessionsQuery exportAuditSessionsQuery;
    private final SessionRestMapper sessionRestMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<PageResponse<SessionAuditResponse>> getAuditSessions(
            @Valid @ModelAttribute GetSessionsRestRequest restRequest) {
        GetSessionsRequest request = sessionRestMapper.toGetSessionsRequest(restRequest);
        PageResponse<SessionAuditResponse> response = getAuditSessionsQuery.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export_excel")
    //@PreAuthorize("hasAuthority('Export_Excel_Audit_Sessions')")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<byte[]> exportSessionsExcel(
            @Valid @RequestBody ExportSessionsRestRequest restRequest) {
        ExportSessionsRequest request = sessionRestMapper.toExportSessionsRequest(restRequest, "EXCEL");
        ExportFileResponse response = exportAuditSessionsQuery.execute(request);
        return buildFileResponse(response);
    }

    @PostMapping("/export_pdf")
    //@PreAuthorize("hasAuthority('Export_PDF_Audit_Sessions')")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<byte[]> exportSessionsPdf(
            @Valid @RequestBody ExportSessionsRestRequest restRequest) {
        ExportSessionsRequest request = sessionRestMapper.toExportSessionsRequest(restRequest, "PDF");
        ExportFileResponse response = exportAuditSessionsQuery.execute(request);
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
