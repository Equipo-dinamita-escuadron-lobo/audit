package com.audit.infrastructure.adpaters.input.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.audit.application.dto.request.ExportDocumentsEventsRequest;
import com.audit.application.dto.request.GetDocumentEventDetailRequest;
import com.audit.application.dto.request.GetDocumentsEventsRequest;
import com.audit.application.dto.response.DocumentEventAuditResponse;
import com.audit.application.dto.response.DocumentEventDetailResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.infrastructure.adapters.input.rest.controller.AuditDocumentEventController;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportDocumentsEventsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetDocumentsEventsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.DocumentEventRestMapper;
import com.audit.application.port.input.queries.GetAuditDocumentEventDetailsQuery;
import com.audit.application.port.input.queries.GetAuditDocumentEventQuery;
import com.audit.application.port.input.queries.export.IDocumentExportUseCase;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditDocumentEventControllerTest {

    private final GetAuditDocumentEventQuery query = mock(GetAuditDocumentEventQuery.class);
    private final GetAuditDocumentEventDetailsQuery detailsQuery = mock(GetAuditDocumentEventDetailsQuery.class);
    private final DocumentEventRestMapper mapper = mock(DocumentEventRestMapper.class);
    private final IDocumentExportUseCase exportUseCase = mock(IDocumentExportUseCase.class);

    private final AuditDocumentEventController controller = new AuditDocumentEventController(query, detailsQuery,
            mapper, exportUseCase);

    @Test
    @DisplayName("getAuditDocumentEvents - debe retornar 200")
    void getAuditDocumentEvents_shouldReturnOk() {
        GetDocumentsEventsRestRequest restRequest = new GetDocumentsEventsRestRequest();
        GetDocumentsEventsRequest appRequest = GetDocumentsEventsRequest.builder().build();

        PageResponse<DocumentEventAuditResponse> page = PageResponse.<DocumentEventAuditResponse>builder()
                .data(List.of())
                .totalElements(0L)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(mapper.toGetDocumentsEventsRequest(restRequest)).thenReturn(appRequest);
        when(query.execute(appRequest)).thenReturn(page);

        ResponseEntity<PageResponse<DocumentEventAuditResponse>> response = controller
                .getAuditDocumentEvents(restRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    @DisplayName("getDocumentEventDetails - debe retornar detalles del documento")
    void getDocumentEventDetails_shouldReturnOk() {
        List<DocumentEventDetailResponse> details = List.of(
                DocumentEventDetailResponse.builder()
                        .operationType("CREATE")
                        .userName("Freider")
                        .build());

        when(detailsQuery.execute(any(GetDocumentEventDetailRequest.class))).thenReturn(details);

        ResponseEntity<List<DocumentEventDetailResponse>> response = controller.getDocumentEventDetails("FAC-001",
                "ENT-001");

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertEquals(1, response.getBody().size()),
                () -> assertEquals("CREATE", response.getBody().get(0).getOperationType()));

        verify(detailsQuery).execute(argThat(req -> req.getDocumentCode().equals("FAC-001")
                && req.getEnterpriseId().equals("ENT-001")));
    }

    @Test
    @DisplayName("exportDocuments - debe retornar 202 con jobId")
    void exportDocuments_shouldReturnAccepted() {
        ExportDocumentsEventsRestRequest restRequest = new ExportDocumentsEventsRestRequest();
        ExportDocumentsEventsRequest appRequest = ExportDocumentsEventsRequest.builder().build();

        when(mapper.toExportDocumentsEventsRequest(restRequest)).thenReturn(appRequest);
        when(exportUseCase.execute(appRequest)).thenReturn("JOB-001");

        ResponseEntity<Map<String, String>> response = controller.exportDocuments(restRequest);

        assertAll(
                () -> assertEquals(202, response.getStatusCode().value()),
                () -> assertEquals("JOB-001", response.getBody().get("jobId")));
    }
}
