package com.audit.infrastructure.adpaters.input.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.dto.response.SessionAuditResponse;
import com.audit.infrastructure.adapters.input.rest.controller.AuditSessionController;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.SessionRestMapper;
import com.audit.application.port.input.queries.GetAuditSessionsQuery;
import com.audit.application.port.input.queries.export.ISessionExportUseCase;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditSessionControllerTest {

    private final GetAuditSessionsQuery query = mock(GetAuditSessionsQuery.class);
    private final SessionRestMapper mapper = mock(SessionRestMapper.class);
    private final ISessionExportUseCase exportUseCase = mock(ISessionExportUseCase.class);

    private final AuditSessionController controller = new AuditSessionController(query, mapper, exportUseCase);

    @Test
    @DisplayName("getAuditSessions - debe mapear request, ejecutar query y retornar 200")
    void getAuditSessions_shouldReturnOk() {
        GetSessionsRestRequest restRequest = new GetSessionsRestRequest();
        GetSessionsRequest appRequest = GetSessionsRequest.builder().build();

        PageResponse<SessionAuditResponse> page = PageResponse.<SessionAuditResponse>builder()
                .data(List.of())
                .totalElements(0L)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(mapper.toGetSessionsRequest(restRequest)).thenReturn(appRequest);
        when(query.execute(appRequest)).thenReturn(page);

        ResponseEntity<PageResponse<SessionAuditResponse>> response = controller.getAuditSessions(restRequest);

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertEquals(page, response.getBody()));

        verify(mapper).toGetSessionsRequest(restRequest);
        verify(query).execute(appRequest);
    }

    @Test
    @DisplayName("exportSessions - debe crear job y retornar 202")
    void exportSessions_shouldReturnAcceptedWithJobId() {
        ExportSessionsRestRequest restRequest = new ExportSessionsRestRequest();
        ExportSessionsRequest appRequest = ExportSessionsRequest.builder().build();

        when(mapper.toExportSessionsRequest(restRequest)).thenReturn(appRequest);
        when(exportUseCase.execute(appRequest)).thenReturn("JOB-001");

        ResponseEntity<Map<String, String>> response = controller.exportSessions(restRequest);

        assertAll(
                () -> assertEquals(202, response.getStatusCode().value()),
                () -> assertEquals("JOB-001", response.getBody().get("jobId")));
    }
}
