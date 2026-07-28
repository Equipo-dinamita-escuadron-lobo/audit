package com.audit.infrastructure.adpaters.input.rest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.OperationType;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetOperationsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.OperationRestMapper;
import com.audit.infrastructure.adapters.output.exception.security.MissingHeaderException;
import com.audit.infrastructure.adapters.output.security.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;

class OperationRestMapperTest {

    private final SecurityContextService securityContextService = mock(SecurityContextService.class);
    private final OperationRestMapper mapper = new OperationRestMapper(securityContextService);

    private final ZonedDateTime FROM = ZonedDateTime.parse("2025-01-01T00:00:00Z");
    private final ZonedDateTime TO = ZonedDateTime.parse("2025-01-31T23:59:59Z");

    @Test
    @DisplayName("toGetOperationsRequest - debe mapear todos los campos correctamente")
    void toGetOperationsRequest_shouldMapAllFields() {
        GetOperationsRestRequest rest = GetOperationsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .userName("Freider")
                .userRole("ADMIN")
                .operationType(OperationType.CREATE)
                .registerId("REG-001")
                .enterpriseId("ENT-001")
                .page(0)
                .size(20)
                .sortField("operationAt")
                .sortDirection("DESC")
                .build();

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        GetOperationsRequest result = mapper.toGetOperationsRequest(rest, httpRequest);

        assertAll(
                () -> assertEquals(FROM.toInstant(), result.getDateFrom()),
                () -> assertEquals(TO.toInstant(), result.getDateTo()),
                () -> assertEquals("CONFIGURATION", result.getModuleName()),
                () -> assertEquals("cost_centers", result.getAffectedTable()),
                () -> assertEquals("Freider", result.getUserName()),
                () -> assertEquals("ADMIN", result.getUserRole()),
                () -> assertEquals(OperationType.CREATE, result.getOperationType()),
                () -> assertEquals("REG-001", result.getRegisterId()),
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals(0, result.getPage()),
                () -> assertEquals(20, result.getSize()));
    }

    @Test
    @DisplayName("toGetModulesTablesRequest - sin header debe lanzar MissingHeaderException")
    void toGetModulesTablesRequest_missingHeader_shouldThrow() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("X-Enterprise-Id")).thenReturn(null);

        assertThrows(MissingHeaderException.class,
                () -> mapper.toGetModulesTablesRequest(httpRequest));
    }

    @Test
    @DisplayName("toGetModulesTablesRequest - header en blanco debe lanzar MissingHeaderException")
    void toGetModulesTablesRequest_blankHeader_shouldThrow() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("X-Enterprise-Id")).thenReturn("   ");

        assertThrows(MissingHeaderException.class,
                () -> mapper.toGetModulesTablesRequest(httpRequest));
    }

    @Test
    @DisplayName("toExportOperationsRequest - debe resolver requestedBy desde SecurityContextService")
    void toExportOperationsRequest_shouldResolveRequestedBy() {
        when(securityContextService.getCurrentUsername()).thenReturn("export_user");

        ExportOperationsRestRequest rest = ExportOperationsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .enterpriseId("ENT-001")
                .enterpriseName("Mi Empresa")
                .exportFormat(ExportFormat.EXCEL)
                .build();

        ExportOperationsRequest result = mapper.toExportOperationsRequest(rest, mock(HttpServletRequest.class));

        assertAll(
                () -> assertEquals("export_user", result.getRequestedBy()),
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals(ExportFormat.EXCEL, result.getExportFormat()));

        verify(securityContextService).getCurrentUsername();
    }
}
