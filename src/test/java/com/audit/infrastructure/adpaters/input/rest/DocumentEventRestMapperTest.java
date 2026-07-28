package com.audit.infrastructure.adpaters.input.rest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.dto.request.ExportDocumentsEventsRequest;
import com.audit.application.dto.request.GetDocumentsEventsRequest;
import com.audit.domain.enums.AuditDateType;
import com.audit.domain.enums.ExportFormat;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportDocumentsEventsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetDocumentsEventsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.DocumentEventRestMapper;
import com.audit.infrastructure.adapters.output.security.SecurityContextService;

class DocumentEventRestMapperTest {

    private final SecurityContextService securityContextService = mock(SecurityContextService.class);
    private final DocumentEventRestMapper mapper = new DocumentEventRestMapper(securityContextService);

    private final ZonedDateTime FROM = ZonedDateTime.parse("2025-01-01T00:00:00Z");
    private final ZonedDateTime TO = ZonedDateTime.parse("2025-01-31T23:59:59Z");

    @Test
    @DisplayName("toGetDocumentsEventsRequest - debe convertir ZonedDateTime a Instant y mapear todos los campos")
    void toGetDocumentsEventsRequest_shouldMapAllFields() {
        GetDocumentsEventsRestRequest rest = GetDocumentsEventsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .dateType(AuditDateType.DOCUMENT_DATE)
                .enterpriseId("ENT-001")
                .documentCode("FAC-001")
                .documentType("FACTURA")
                .createdBy("juan")
                .thirdPartyName("Tercero SA")
                .page(1)
                .size(10)
                .sortField("documentDate")
                .sortDirection("ASC")
                .build();

        GetDocumentsEventsRequest result = mapper.toGetDocumentsEventsRequest(rest);

        assertAll(
                () -> assertEquals(FROM.toInstant(), result.getDateFrom()),
                () -> assertEquals(TO.toInstant(), result.getDateTo()),
                () -> assertEquals(AuditDateType.DOCUMENT_DATE, result.getDateType()),
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals("FAC-001", result.getDocumentCode()),
                () -> assertEquals("FACTURA", result.getDocumentType()),
                () -> assertEquals("juan", result.getCreatedBy()),
                () -> assertEquals("Tercero SA", result.getThirdPartyName()),
                () -> assertEquals(1, result.getPage()),
                () -> assertEquals(10, result.getSize()),
                () -> assertEquals("documentDate", result.getSortField()),
                () -> assertEquals("ASC", result.getSortDirection()));
    }

    @Test
    @DisplayName("toExportDocumentsEventsRequest - debe resolver requestedBy desde SecurityContextService")
    void toExportDocumentsEventsRequest_shouldResolveRequestedByFromSecurityContext() {
        when(securityContextService.getCurrentUsername()).thenReturn("admin_user");

        ExportDocumentsEventsRestRequest rest = ExportDocumentsEventsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .enterpriseId("ENT-001")
                .enterpriseName("Mi Empresa")
                .exportFormat(ExportFormat.EXCEL)
                .build();

        ExportDocumentsEventsRequest result = mapper.toExportDocumentsEventsRequest(rest);

        assertAll(
                () -> assertEquals("admin_user", result.getRequestedBy()),
                () -> assertEquals(FROM.toInstant(), result.getDateFrom()),
                () -> assertEquals(TO.toInstant(), result.getDateTo()),
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals("Mi Empresa", result.getEnterpriseName()),
                () -> assertEquals(ExportFormat.EXCEL, result.getExportFormat()));

        verify(securityContextService).getCurrentUsername();
    }

    @Test
    @DisplayName("toExportDocumentsEventsRequest - campos opcionales nulos deben quedar nulos")
    void toExportDocumentsEventsRequest_optionalFieldsNull_shouldBeNull() {
        when(securityContextService.getCurrentUsername()).thenReturn("admin_user");

        ExportDocumentsEventsRestRequest rest = ExportDocumentsEventsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .enterpriseId("ENT-001")
                .enterpriseName("Mi Empresa")
                .build();

        ExportDocumentsEventsRequest result = mapper.toExportDocumentsEventsRequest(rest);

        assertAll(
                () -> assertNull(result.getDocumentCode()),
                () -> assertNull(result.getDocumentType()),
                () -> assertNull(result.getThirdPartyName()),
                () -> assertNull(result.getOperationType()),
                () -> assertNull(result.getUserName()));
    }
}
