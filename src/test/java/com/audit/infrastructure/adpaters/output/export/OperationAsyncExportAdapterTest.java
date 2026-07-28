package com.audit.infrastructure.adpaters.output.export;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.internal.ExportJobTracker;
import com.audit.application.internal.query.AuditOperationCriteria;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportJobStatus;
import com.audit.domain.enums.ExportType;
import com.audit.domain.enums.OperationType;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.ExportJob;
import com.audit.domain.model.OperationData;
import com.audit.application.port.output.AuditOperationQueryPort;
import com.audit.infrastructure.adapters.output.export.adapter.OperationAsyncExportAdapter;
import com.audit.infrastructure.adapters.output.export.generator.OperationExcelGenerator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OperationAsyncExportAdapterTest {

    private final AuditOperationQueryPort operationQueryPort = mock(AuditOperationQueryPort.class);
    private final ExportJobTracker jobTracker = mock(ExportJobTracker.class);
    private final OperationExcelGenerator excelGenerator = mock(OperationExcelGenerator.class);

    private final OperationAsyncExportAdapter adapter = new OperationAsyncExportAdapter(operationQueryPort, jobTracker,
            excelGenerator);

    private AuditOperation operation() {
        Instant now = Instant.now();

        return AuditOperation.reconstruct(
                1L,
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                OperationType.CREATE,
                now,
                "CONFIGURATION",
                "cost_centers",
                "1",
                "ENT-001",
                OperationData.forCreate(Map.of("id", 1L, "name", "Centro")),
                now);
    }

    private ExportOperationsRequest request() {
        return ExportOperationsRequest.builder()
                .requestedBy("Freider")
                .enterpriseId("ENT-001")
                .enterpriseName("Empresa prueba")
                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                .dateTo(Instant.now())
                .userName("Freider")
                .userRole("ADMIN")
                .operationType(OperationType.CREATE)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .exportFormat(ExportFormat.EXCEL)
                .build();
    }

    @Test
    @DisplayName("process - con datos debe generar Excel y completar job")
    void process_withOperations_shouldCompleteJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "operations_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        ExportOperationsRequest request = request();

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(operationQueryPort.findAllForExport(any())).thenReturn(List.of(operation()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("archivo".getBytes());

        adapter.process(request, "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.COMPLETED, job.getStatus()),
                () -> assertEquals(100, job.getProgress()),
                () -> assertEquals(1, job.getTotalRecords()),
                () -> assertArrayEquals("archivo".getBytes(), job.getFileData()),
                () -> assertTrue(job.isCompleted()));

        verify(operationQueryPort).findAllForExport(any());
        verify(excelGenerator).generate(
                anyList(),
                eq("Empresa prueba"),
                eq("Freider"),
                anyString(),
                contains("Usuario: Freider"));
    }

    @Test
    @DisplayName("process - sin operaciones debe marcar job como fallido")
    void process_withoutOperations_shouldFailJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "operations_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(operationQueryPort.findAllForExport(any())).thenReturn(List.of());

        adapter.process(request(), "JOB-001");

        assertAll(
                () -> assertEquals(ExportJobStatus.FAILED, job.getStatus()),
                () -> assertEquals("No se encontraron registros para los filtros aplicados", job.getErrorMessage()),
                () -> assertTrue(job.isFailed()));

        verify(excelGenerator, never()).generate(anyList(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - si el generador falla debe marcar job como fallido")
    void process_generatorThrows_shouldFailJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "operations_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(operationQueryPort.findAllForExport(any())).thenReturn(List.of(operation()));
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

        verify(operationQueryPort, never()).findAllForExport(any());
        verify(excelGenerator, never()).generate(anyList(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("process - debe construir criterio con datos del request")
    void process_shouldBuildCriteriaFromRequest() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "operations_audit.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        when(jobTracker.getJob("JOB-001")).thenReturn(Optional.of(job));
        when(operationQueryPort.findAllForExport(any())).thenReturn(List.of(operation()));
        when(excelGenerator.generate(anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("archivo".getBytes());

        adapter.process(request(), "JOB-001");

        ArgumentCaptor<AuditOperationCriteria> captor = ArgumentCaptor.forClass(AuditOperationCriteria.class);

        verify(operationQueryPort).findAllForExport(captor.capture());

        var criteria = captor.getValue();

        assertAll(
                () -> assertEquals("ENT-001", criteria.getEnterpriseId()),
                () -> assertEquals("CONFIGURATION", criteria.getModuleName()),
                () -> assertEquals("cost_centers", criteria.getAffectedTable()),
                () -> assertEquals("Freider", criteria.getUserName()),
                () -> assertEquals("ADMIN", criteria.getUserRole()),
                () -> assertEquals(OperationType.CREATE, criteria.getOperationType()),
                () -> assertEquals("1", criteria.getRegisterId()));
    }
}
