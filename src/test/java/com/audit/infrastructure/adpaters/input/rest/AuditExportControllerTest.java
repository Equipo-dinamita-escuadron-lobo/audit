package com.audit.infrastructure.adpaters.input.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.audit.application.dto.response.ExportFileResult;
import com.audit.application.dto.response.ExportJobResponse;
import com.audit.application.port.input.queries.export.IExportJobQuery;
import com.audit.domain.enums.ExportFormat;
import com.audit.infrastructure.adapters.input.rest.controller.AuditExportController;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditExportControllerTest {

    private final IExportJobQuery exportJobQuery = mock(IExportJobQuery.class);
    private final AuditExportController controller = new AuditExportController(exportJobQuery);

    @Test
    @DisplayName("getExportStatus - si job existe debe retornar 200")
    void getExportStatus_existingJob_shouldReturnOk() {
        ExportJobResponse response = ExportJobResponse.builder()
                .jobId("JOB-001")
                .status("COMPLETED")
                .progress(100)
                .build();

        when(exportJobQuery.getJobStatus("JOB-001")).thenReturn(Optional.of(response));

        ResponseEntity<ExportJobResponse> result = controller.getExportStatus("JOB-001");

        assertAll(
                () -> assertEquals(200, result.getStatusCode().value()),
                () -> assertEquals("JOB-001", result.getBody().getJobId()),
                () -> assertEquals("COMPLETED", result.getBody().getStatus()));
    }

    @Test
    @DisplayName("getExportStatus - si job no existe debe lanzar 404")
    void getExportStatus_notFound_shouldThrow404() {
        when(exportJobQuery.getJobStatus("NO-EXISTE")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getExportStatus("NO-EXISTE"));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadExport - PDF debe retornar application/pdf")
    void downloadExport_pdf_shouldReturnPdfResponse() {
        byte[] data = "pdf".getBytes();

        when(exportJobQuery.getJobFileData("JOB-001"))
                .thenReturn(ExportFileResult.builder()
                        .data(data)
                        .fileName("auditoria.pdf")
                        .format(ExportFormat.PDF)
                        .build());

        ResponseEntity<byte[]> response = controller.downloadExport("JOB-001");

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertEquals("application/pdf", response.getHeaders().getContentType().toString()),
                () -> assertArrayEquals(data, response.getBody()),
                () -> assertTrue(response.getHeaders().getFirst("Content-Disposition")
                        .contains("auditoria.pdf")));
    }

    @Test
    @DisplayName("downloadExport - EXCEL debe retornar media type de xlsx")
    void downloadExport_excel_shouldReturnExcelResponse() {
        byte[] data = "excel".getBytes();

        when(exportJobQuery.getJobFileData("JOB-001"))
                .thenReturn(ExportFileResult.builder()
                        .data(data)
                        .fileName("auditoria.xlsx")
                        .format(ExportFormat.EXCEL)
                        .build());

        ResponseEntity<byte[]> response = controller.downloadExport("JOB-001");

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertEquals(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        response.getHeaders().getContentType().toString()),
                () -> assertArrayEquals(data, response.getBody()));
    }

    @Test
    @DisplayName("removeExportJob - debe eliminar job y retornar 204")
    void removeExportJob_shouldReturnNoContent() {
        ResponseEntity<Void> response = controller.removeExportJob("JOB-001");

        assertEquals(204, response.getStatusCode().value());
        verify(exportJobQuery).removeJob("JOB-001");
    }
}
