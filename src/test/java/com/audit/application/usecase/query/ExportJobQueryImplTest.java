package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.response.ExportFileResult;
import com.audit.application.dto.response.ExportJobResponse;
import com.audit.application.internal.ExportJobTracker;
import com.audit.application.usecases.queries.export.ExportJobQueryImpl;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportType;
import com.audit.domain.exceptions.ExportAuditException;
import com.audit.domain.model.ExportJob;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportJobQueryImplTest {

    @Mock
    private ExportJobTracker jobTracker;

    @InjectMocks
    private ExportJobQueryImpl useCase;

    @Test
    @DisplayName("getJobStatus - debe retornar estado del job cuando existe")
    void getJobStatus_existingJob_shouldReturnResponse() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "auditoria.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        when(jobTracker.getJob(job.getJobId())).thenReturn(Optional.of(job));

        Optional<ExportJobResponse> response = useCase.getJobStatus(job.getJobId());

        assertAll(
                () -> assertTrue(response.isPresent()),
                () -> assertEquals(job.getJobId(), response.get().getJobId()),
                () -> assertEquals("PENDING", response.get().getStatus()),
                () -> assertEquals("auditoria.xlsx", response.get().getFileName()),
                () -> assertEquals("OPERATION", response.get().getExportType()),
                () -> assertEquals("EXCEL", response.get().getExportFormat()));
    }

    @Test
    @DisplayName("getJobStatus - debe retornar vacío si no existe")
    void getJobStatus_notFound_shouldReturnEmpty() {
        when(jobTracker.getJob("NO-EXISTE")).thenReturn(Optional.empty());

        Optional<ExportJobResponse> response = useCase.getJobStatus("NO-EXISTE");

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("getJobFileData - job completado debe retornar archivo")
    void getJobFileData_completedJob_shouldReturnFile() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "auditoria.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        byte[] data = "archivo".getBytes();
        job.markAsProcessing();
        job.complete(data, 10);

        when(jobTracker.getJob(job.getJobId())).thenReturn(Optional.of(job));

        ExportFileResult result = useCase.getJobFileData(job.getJobId());

        assertAll(
                () -> assertArrayEquals(data, result.getData()),
                () -> assertEquals("auditoria.xlsx", result.getFileName()),
                () -> assertEquals(ExportFormat.EXCEL, result.getFormat()));
    }

    @Test
    @DisplayName("getJobFileData - debe fallar si job no existe")
    void getJobFileData_notFound_throwsException() {
        when(jobTracker.getJob("NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(ExportAuditException.class, () -> useCase.getJobFileData("NO-EXISTE"));
    }

    @Test
    @DisplayName("getJobFileData - debe fallar si job está pendiente")
    void getJobFileData_pendingJob_throwsException() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "auditoria.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        when(jobTracker.getJob(job.getJobId())).thenReturn(Optional.of(job));

        assertThrows(ExportAuditException.class, () -> useCase.getJobFileData(job.getJobId()));
    }

    @Test
    @DisplayName("getJobFileData - debe fallar si job falló")
    void getJobFileData_failedJob_throwsException() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "auditoria.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        job.markAsProcessing();
        job.fail("Error generando archivo");

        when(jobTracker.getJob(job.getJobId())).thenReturn(Optional.of(job));

        assertThrows(ExportAuditException.class, () -> useCase.getJobFileData(job.getJobId()));
    }

    @Test
    @DisplayName("removeJob - debe delegar eliminación al tracker")
    void removeJob_shouldDelegateToTracker() {
        useCase.removeJob("JOB-001");

        verify(jobTracker).removeJob("JOB-001");
    }
}
