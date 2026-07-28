package com.audit.infrastructure.adpaters.input.rest;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.request.GetModulesTablesRequest;
import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.application.dto.response.ModuleTableResponse;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.GetAuditOperationsQuery;
import com.audit.infrastructure.adapters.input.rest.controller.AuditOperationController;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.OperationRestMapper;
import com.audit.application.port.input.queries.GetModulesAndTablesQuery;
import com.audit.application.port.input.queries.export.IOperationExportUseCase;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditOperationControllerTest {

    private final GetAuditOperationsQuery operationsQuery = mock(GetAuditOperationsQuery.class);
    private final GetModulesAndTablesQuery modulesQuery = mock(GetModulesAndTablesQuery.class);
    private final OperationRestMapper mapper = mock(OperationRestMapper.class);
    private final IOperationExportUseCase exportUseCase = mock(IOperationExportUseCase.class);

    private final AuditOperationController controller = new AuditOperationController(operationsQuery, modulesQuery,
            mapper, exportUseCase);

    @Test
    @DisplayName("getAuditOperations - debe retornar 200")
    void getAuditOperations_shouldReturnOk() {
        GetOperationsRestRequest restRequest = new GetOperationsRestRequest();
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        GetOperationsRequest appRequest = GetOperationsRequest.builder().build();

        PageResponse<OperationAuditResponse> page = PageResponse.<OperationAuditResponse>builder()
                .data(List.of())
                .totalElements(0L)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(mapper.toGetOperationsRequest(restRequest, httpRequest)).thenReturn(appRequest);
        when(operationsQuery.execute(appRequest)).thenReturn(page);

        ResponseEntity<PageResponse<OperationAuditResponse>> response = controller.getAuditOperations(restRequest,
                httpRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    @DisplayName("getModulesAndTables - debe retornar módulos y tablas")
    void getModulesAndTables_shouldReturnOk() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        GetModulesTablesRequest appRequest = new GetModulesTablesRequest("ENT-001");

        List<ModuleTableResponse> result = List.of(
                new ModuleTableResponse("CONFIGURATION", "cost_centers"));

        when(mapper.toGetModulesTablesRequest(httpRequest)).thenReturn(appRequest);
        when(modulesQuery.execute(appRequest)).thenReturn(result);

        ResponseEntity<List<ModuleTableResponse>> response = controller.getModulesAndTables(httpRequest);

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertEquals(1, response.getBody().size()),
                () -> assertEquals("CONFIGURATION", response.getBody().get(0).moduleName()));
    }

    @Test
    @DisplayName("exportOperations - debe retornar 202 con jobId")
    void exportOperations_shouldReturnAccepted() {
        ExportOperationsRestRequest restRequest = new ExportOperationsRestRequest();
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        ExportOperationsRequest appRequest = ExportOperationsRequest.builder().build();

        when(mapper.toExportOperationsRequest(restRequest, httpRequest)).thenReturn(appRequest);
        when(exportUseCase.execute(appRequest)).thenReturn("JOB-001");

        ResponseEntity<Map<String, String>> response = controller.exportOperations(restRequest, httpRequest);

        assertAll(
                () -> assertEquals(202, response.getStatusCode().value()),
                () -> assertEquals("JOB-001", response.getBody().get("jobId")));
    }
}
