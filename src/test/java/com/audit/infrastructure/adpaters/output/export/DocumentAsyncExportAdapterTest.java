package com.audit.infrastructure.adpaters.output.export;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.dto.request.ExportDocumentsEventsRequest;
import com.audit.application.internal.ExportJobTracker;
import com.audit.application.internal.query.AuditDocumentExportCriteria;
import com.audit.application.port.output.AuditDocumentEventQueryPort;
import com.audit.domain.enums.AuditDateType;
import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportJobStatus;
import com.audit.domain.enums.ExportType;
import com.audit.domain.model.AuditDocumentEvent;
import com.audit.domain.model.DocumentData;
import com.audit.domain.model.ExportJob;
import com.audit.infrastructure.adapters.output.export.adapter.DocumentAsyncExportAdapter;
import com.audit.infrastructure.adapters.output.export.generator.DocumentExcelGenerator;

import org.mockito.ArgumentCaptor;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentAsyncExportAdapterTest {

    private final AuditDocumentEventQueryPort documentQueryPort = mock(AuditDocumentEventQueryPort.class);
    private final ExportJobTracker jobTracker = mock(ExportJobTracker.class);
    private final DocumentExcelGenerator excelGenerator = mock(DocumentExcelGenerator.class);

    private final DocumentAsyncExportAdapter adapter = new DocumentAsyncExportAdapter(documentQueryPort, jobTracker,
            excelGenerator);

    private AuditDocumentEvent documentEvent() {
        Instant now = Instant.now();

        DocumentData data = DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                List.of(Map.of("account", "1105")),
                Map.of("total", 1000),
                Map.of("source", "sales"));

        return AuditDocumentEvent.reconstruct(
                1L,
                "ENT-001",
                "FAC-001",
                "FACTURA",
                "DOC-001",
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                DocumentOperationType.CREATE,
                "TP-001",
                "Cliente prueba",
                "DOCUMENTS",
                now,
                LocalDate.now(),
                data,
                now);
    }

    private ExportDocumentsEventsRequest request() {
        return ExportDocumentsEventsRequest.builder()
                .requestedBy("Freider")
                .enterpriseId("ENT-001")
                .enterpriseName("Empresa prueba")
                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                .dateTo(Instant.now())
                .dateType(AuditDateType.DOCUMENT_DATE)
                .documentCode("FAC-001")
                .documentType("FACTURA")
                .thirdPartyName("Cliente")
                .operationType(DocumentOperationType.CREATE)
                .userName("Freider")
                .exportFormat(ExportFormat.EXCEL)
                .build();
    }

    @Test
    @DisplayName("process - con eventos debe generar Excel y completar job")
    void process_withEvents_shouldCompleteJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "documents_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.DOCUMENT);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(documentQueryPort.findAllEventsForExport(any())).thenReturn(List.of(documentEvent()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("archivo".getBytes());

        adapter.process(request(), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.COMPLETED, job.getStatus()),
                () -> assertEquals(100, job.getProgress()),
                () -> assertEquals(1, job.getTotalRecords()),
                () -> assertArrayEquals("archivo".getBytes(), job.getFileData()),
                () -> assertTrue(job.isCompleted()));

        verify(documentQueryPort).findAllEventsForExport(any());
        verify(excelGenerator).generate(
                anyList(),
                eq("Empresa prueba"),
                eq("Freider"),
                anyString(),
                contains("Usuario: Freider"));
    }

    @Test
    @DisplayName("process - sin eventos debe marcar job como fallido")
    void process_withoutEvents_shouldFailJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "documents_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.DOCUMENT);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(documentQueryPort.findAllEventsForExport(any())).thenReturn(List.of());

        adapter.process(request(), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.FAILED, job.getStatus()),
                () -> assertEquals("No se encontraron eventos para los filtros aplicados", job.getErrorMessage()),
                () -> assertTrue(job.isFailed()));

        verify(excelGenerator, never()).generate(anyList(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - si el generador falla debe marcar job como fallido")
    void process_generatorThrows_shouldFailJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "documents_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.DOCUMENT);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(documentQueryPort.findAllEventsForExport(any())).thenReturn(List.of(documentEvent()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Error Excel"));

        adapter.process(request(), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.FAILED, job.getStatus()),
                () -> assertTrue(job.getErrorMessage().contains("Error inesperado")),
                () -> assertTrue(job.getErrorMessage().contains("Error Excel")));
    }

    @Test
    @DisplayName("process - si el job no existe no debe propagar excepción")
    void process_jobNotFound_shouldNotPropagateException() {
        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> adapter.process(request(), "JOB-001"));

        verify(documentQueryPort, never()).findAllEventsForExport(any());
        verify(excelGenerator, never()).generate(anyList(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - debe construir criterio documental con datos del request")
    void process_shouldBuildDocumentCriteriaFromRequest() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "documents_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.DOCUMENT);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(documentQueryPort.findAllEventsForExport(any())).thenReturn(List.of(documentEvent()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("archivo".getBytes());

        adapter.process(request(), "JOB-001");

        ArgumentCaptor<AuditDocumentExportCriteria> captor = ArgumentCaptor
                .forClass(AuditDocumentExportCriteria.class);

        verify(documentQueryPort).findAllEventsForExport(captor.capture());

        var criteria = captor.getValue();

        assertAll(
                () -> assertEquals("ENT-001", criteria.getEnterpriseId()),
                () -> assertEquals(AuditDateType.DOCUMENT_DATE, criteria.getDateType()),
                () -> assertEquals("FAC-001", criteria.getDocumentCode()),
                () -> assertEquals("FACTURA", criteria.getDocumentType()),
                () -> assertEquals("Cliente", criteria.getThirdPartyName()),
                () -> assertEquals(DocumentOperationType.CREATE, criteria.getOperationType()),
                () -> assertEquals("Freider", criteria.getUserName()));
    }

    @Test
    @DisplayName("process - filtros vacíos debe enviar cadena de filtros vacía")
    void process_withoutFilters_shouldSendEmptyAppliedFilters() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "documents_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.DOCUMENT);

        ExportDocumentsEventsRequest request = ExportDocumentsEventsRequest.builder()
                .requestedBy("Freider")
                .enterpriseId("ENT-001")
                .enterpriseName("Empresa prueba")
                .exportFormat(ExportFormat.EXCEL)
                .build();

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(documentQueryPort.findAllEventsForExport(any())).thenReturn(List.of(documentEvent()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("archivo".getBytes());

        adapter.process(request, "JOB-001");

        verify(excelGenerator).generate(
                anyList(),
                eq("Empresa prueba"),
                eq("Freider"),
                anyString(),
                eq(""));
    }
}
