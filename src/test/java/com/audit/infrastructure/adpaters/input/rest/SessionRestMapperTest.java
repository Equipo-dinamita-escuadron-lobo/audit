package com.audit.infrastructure.adpaters.input.rest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.UserAction;
import com.audit.infrastructure.adapters.input.rest.dto.request.ExportSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.dto.request.GetSessionsRestRequest;
import com.audit.infrastructure.adapters.input.rest.mapper.SessionRestMapper;
import com.audit.infrastructure.adapters.output.security.SecurityContextService;

class SessionRestMapperTest {

    private final SecurityContextService securityContextService = mock(SecurityContextService.class);
    private final SessionRestMapper mapper = new SessionRestMapper(securityContextService);

    private final ZonedDateTime FROM = ZonedDateTime.parse("2025-01-01T00:00:00Z");
    private final ZonedDateTime TO = ZonedDateTime.parse("2025-01-31T23:59:59Z");

    @Test
    @DisplayName("toGetSessionsRequest - debe inyectar requestingUserRole desde SecurityContextService")
    void toGetSessionsRequest_shouldInjectRequestingUserRole() {
        when(securityContextService.getCurrentUserRole()).thenReturn("Administrador");

        GetSessionsRestRequest rest = GetSessionsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .userName("juan")
                .userRole("Profesor")
                .action(UserAction.LOGIN)
                .page(0)
                .size(20)
                .sortField("actionAt")
                .sortDirection("DESC")
                .build();

        GetSessionsRequest result = mapper.toGetSessionsRequest(rest);

        assertAll(
                () -> assertEquals(FROM.toInstant(), result.getDateFrom()),
                () -> assertEquals(TO.toInstant(), result.getDateTo()),
                () -> assertEquals("juan", result.getUserName()),
                () -> assertEquals("Profesor", result.getUserRole()),
                () -> assertEquals(UserAction.LOGIN, result.getAction()),
                () -> assertEquals("Administrador", result.getRequestingUserRole()),
                () -> assertEquals(0, result.getPage()),
                () -> assertEquals(20, result.getSize()));

        verify(securityContextService).getCurrentUserRole();
    }

    @Test
    @DisplayName("toGetSessionsRequest - campos opcionales null deben quedar null")
    void toGetSessionsRequest_optionalFieldsNull_shouldBeNull() {
        when(securityContextService.getCurrentUserRole()).thenReturn("Administrador");

        GetSessionsRestRequest rest = GetSessionsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .build();

        GetSessionsRequest result = mapper.toGetSessionsRequest(rest);

        assertAll(
                () -> assertNull(result.getUserName()),
                () -> assertNull(result.getUserRole()),
                () -> assertNull(result.getAction()));
    }

    @Test
    @DisplayName("toExportSessionsRequest - debe resolver requestedBy desde SecurityContextService")
    void toExportSessionsRequest_shouldResolveRequestedBy() {
        when(securityContextService.getCurrentUsername()).thenReturn("export_admin");

        ExportSessionsRestRequest rest = ExportSessionsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .exportFormat(ExportFormat.PDF)
                .build();

        ExportSessionsRequest result = mapper.toExportSessionsRequest(rest);

        assertAll(
                () -> assertEquals("export_admin", result.getRequestedBy()),
                () -> assertEquals(FROM.toInstant(), result.getDateFrom()),
                () -> assertEquals(TO.toInstant(), result.getDateTo()),
                () -> assertEquals(ExportFormat.PDF, result.getExportFormat()));

        verify(securityContextService).getCurrentUsername();
    }

    @Test
    @DisplayName("toExportSessionsRequest - debe propagar userName y userRole si están presentes")
    void toExportSessionsRequest_withOptionalFields_shouldMapCorrectly() {
        when(securityContextService.getCurrentUsername()).thenReturn("admin");

        ExportSessionsRestRequest rest = ExportSessionsRestRequest.builder()
                .dateFrom(FROM)
                .dateTo(TO)
                .userName("freider")
                .userRole("ADMIN")
                .action(UserAction.LOGOUT)
                .exportFormat(ExportFormat.EXCEL)
                .build();

        ExportSessionsRequest result = mapper.toExportSessionsRequest(rest);

        assertAll(
                () -> assertEquals("freider", result.getUserName()),
                () -> assertEquals("ADMIN", result.getUserRole()),
                () -> assertEquals(UserAction.LOGOUT, result.getAction()));
    }
}
