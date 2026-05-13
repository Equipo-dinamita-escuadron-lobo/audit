package com.audit.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import com.audit.application.port.input.queries.GetAuditDocumentEventQuery;
import com.audit.application.port.input.queries.export.IDocumentExportUseCase;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportDocumentsEventsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetDocumentsEventsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.DocumentEventRestMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.audit.application.dto.request.ExportDocumentsEventsRequest;
import com.audit.application.dto.request.GetDocumentEventDetailRequest;
import com.audit.application.dto.request.GetDocumentsEventsRequest;
import com.audit.application.dto.response.DocumentEventAuditResponse;
import com.audit.application.dto.response.DocumentEventDetailResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.GetAuditDocumentEventDetailsQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/audit/documents")
@RequiredArgsConstructor
@Slf4j
public class AuditDocumentEventController {

    private final GetAuditDocumentEventQuery getAuditDocumentEventQuery;
    private final GetAuditDocumentEventDetailsQuery getAuditDocumentEventDetailsQuery;
    private final DocumentEventRestMapper documentEventRestMapper;
    private final IDocumentExportUseCase documentExportUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador','Profesor','Estudiante')")
    public ResponseEntity<PageResponse<DocumentEventAuditResponse>> getAuditDocumentEvents(
            @Valid @ModelAttribute GetDocumentsEventsRestRequest restRequest) {
        GetDocumentsEventsRequest request = documentEventRestMapper.toGetDocumentsEventsRequest(restRequest);
        PageResponse<DocumentEventAuditResponse> response = getAuditDocumentEventQuery.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentCode}/details")
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<List<DocumentEventDetailResponse>> getDocumentEventDetails(
            @PathVariable String documentCode,
            @NotNull @RequestParam String enterpriseId) {
        GetDocumentEventDetailRequest request = GetDocumentEventDetailRequest.builder()
                .documentCode(documentCode)
                .enterpriseId(enterpriseId)
                .build();
        List<DocumentEventDetailResponse> response = getAuditDocumentEventDetailsQuery.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<Map<String, String>> exportDocuments(
            @Valid @RequestBody ExportDocumentsEventsRestRequest restRequest) {
        ExportDocumentsEventsRequest request = documentEventRestMapper
                .toExportDocumentsEventsRequest(restRequest);
        String jobId = documentExportUseCase.execute(request);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

}
