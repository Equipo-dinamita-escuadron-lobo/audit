package com.audit.infrastructure.adpaters.output.export;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.ExportJobTracker;
import com.audit.application.internal.query.AuditSessionCriteria;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportJobStatus;
import com.audit.domain.enums.ExportType;
import com.audit.domain.enums.UserAction;
import com.audit.domain.model.ExportJob;
import com.audit.infrastructure.adapters.output.export.adapter.SessionAsyncExportAdapter;
import com.audit.infrastructure.adapters.output.export.generator.SessionExcelGenerator;
import com.audit.infrastructure.adapters.output.export.generator.SessionPdfGenerator;
import com.audit.application.port.output.AuditSessionQueryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionAsyncExportAdapterTest {

    private final AuditSessionQueryPort sessionQueryPort = mock(AuditSessionQueryPort.class);
    private final ExportJobTracker jobTracker = mock(ExportJobTracker.class);
    private final SessionExcelGenerator excelGenerator = mock(SessionExcelGenerator.class);
    private final SessionPdfGenerator pdfGenerator = mock(SessionPdfGenerator.class);

    private final SessionAsyncExportAdapter adapter = new SessionAsyncExportAdapter(sessionQueryPort, jobTracker,
            excelGenerator, pdfGenerator);

    private CombinedSession session() {
        return CombinedSession.of(
                "SESSION-001",
                "Freider",
                List.of("ADMIN"),
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now());
    }

    private ExportSessionsRequest request(ExportFormat format) {
        return ExportSessionsRequest.builder()
                .requestedBy("Freider")
                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                .dateTo(Instant.now())
                .userName("Freider")
                .userRole("ADMIN")
                .action(UserAction.LOGIN)
                .exportFormat(format)
                .build();
    }

    @Test
    @DisplayName("process - formato EXCEL con sesiones debe generar archivo y completar job")
    void process_excelWithSessions_shouldCompleteJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "sessions.xlsx",
                ExportFormat.EXCEL,
                ExportType.SESSION);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(sessionQueryPort.findAllForExport(any())).thenReturn(List.of(session()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString()))
                .thenReturn("excel".getBytes());

        adapter.process(request(ExportFormat.EXCEL), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.COMPLETED, job.getStatus()),
                () -> assertEquals(100, job.getProgress()),
                () -> assertEquals(1, job.getTotalRecords()),
                () -> assertArrayEquals("excel".getBytes(), job.getFileData()),
                () -> assertTrue(job.isCompleted()));

        verify(excelGenerator).generate(
                anyList(),
                eq("Freider"),
                anyString(),
                contains("Usuario: Freider"));

        verify(pdfGenerator, never()).generate(anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - formato PDF con sesiones debe generar archivo y completar job")
    void process_pdfWithSessions_shouldCompleteJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "sessions.pdf",
                ExportFormat.PDF,
                ExportType.SESSION);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(sessionQueryPort.findAllForExport(any())).thenReturn(List.of(session()));
        when(pdfGenerator.generate(anyList(), anyString(), anyString(), anyString()))
                .thenReturn("pdf".getBytes());

        adapter.process(request(ExportFormat.PDF), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.COMPLETED, job.getStatus()),
                () -> assertEquals(100, job.getProgress()),
                () -> assertEquals(1, job.getTotalRecords()),
                () -> assertArrayEquals("pdf".getBytes(), job.getFileData()));

        verify(pdfGenerator).generate(
                anyList(),
                eq("Freider"),
                anyString(),
                contains("Usuario: Freider"));

        verify(excelGenerator, never()).generate(anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - sin sesiones debe marcar job como fallido")
    void process_withoutSessions_shouldFailJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "sessions.xlsx",
                ExportFormat.EXCEL,
                ExportType.SESSION);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(sessionQueryPort.findAllForExport(any())).thenReturn(List.of());

        adapter.process(request(ExportFormat.EXCEL), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.FAILED, job.getStatus()),
                () -> assertEquals("No se encontraron registros para los filtros aplicados", job.getErrorMessage()),
                () -> assertTrue(job.isFailed()));

        verify(excelGenerator, never()).generate(anyList(), anyString(), anyString(), anyString());
        verify(pdfGenerator, never()).generate(anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - si el generador falla debe marcar job como fallido")
    void process_generatorThrows_shouldFailJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "sessions.xlsx",
                ExportFormat.EXCEL,
                ExportType.SESSION);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(sessionQueryPort.findAllForExport(any())).thenReturn(List.of(session()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Error Excel"));

        adapter.process(request(ExportFormat.EXCEL), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.FAILED, job.getStatus()),
                () -> assertTrue(job.getErrorMessage().contains("Error inesperado")),
                () -> assertTrue(job.getErrorMessage().contains("Error Excel")));
    }

    @Test
    @DisplayName("process - si el job no existe no debe propagar excepción")
    void process_jobNotFound_shouldNotPropagateException() {
        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> adapter.process(request(ExportFormat.EXCEL), "JOB-001"));

        verify(sessionQueryPort, never()).findAllForExport(any());
        verify(excelGenerator, never()).generate(anyList(), anyString(), anyString(), anyString());
        verify(pdfGenerator, never()).generate(anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - debe construir criterio de sesiones desde el request")
    void process_shouldBuildCriteriaFromRequest() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "sessions.xlsx",
                ExportFormat.EXCEL,
                ExportType.SESSION);

        ExportSessionsRequest request = request(ExportFormat.EXCEL);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(sessionQueryPort.findAllForExport(any())).thenReturn(List.of(session()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString()))
                .thenReturn("excel".getBytes());

        adapter.process(request, "JOB-001");

        ArgumentCaptor<AuditSessionCriteria> captor = ArgumentCaptor.forClass(AuditSessionCriteria.class);

        verify(sessionQueryPort).findAllForExport(captor.capture());

        AuditSessionCriteria criteria = captor.getValue();

        assertAll(
                () -> assertEquals(request.getDateFrom(), criteria.getDateFrom()),
                () -> assertEquals(request.getDateTo(), criteria.getDateTo()),
                () -> assertEquals("Freider", criteria.getUserName()),
                () -> assertEquals("ADMIN", criteria.getUserRole()),
                () -> assertEquals(UserAction.LOGIN, criteria.getAction()));
    }

    @Test
    @DisplayName("process - filtros vacíos debe enviar cadena de filtros vacía")
    void process_withoutFilters_shouldSendEmptyAppliedFilters() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "sessions.xlsx",
                ExportFormat.EXCEL,
                ExportType.SESSION);

        ExportSessionsRequest request = ExportSessionsRequest.builder()
                .requestedBy("Freider")
                .dateFrom(Instant.now().minus(1, ChronoUnit.DAYS))
                .dateTo(Instant.now())
                .exportFormat(ExportFormat.EXCEL)
                .build();

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(sessionQueryPort.findAllForExport(any())).thenReturn(List.of(session()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString()))
                .thenReturn("excel".getBytes());

        adapter.process(request, "JOB-001");

        verify(excelGenerator).generate(
                anyList(),
                eq("Freider"),
                anyString(),
                argThat(filters -> filters.contains("Desde:") && filters.contains("Hasta:")));
    }
}
