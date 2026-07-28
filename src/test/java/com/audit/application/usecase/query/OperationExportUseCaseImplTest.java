package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.internal.ExportJobTracker;
import com.audit.application.port.output.IOperationAsyncExportPort;
import com.audit.application.usecases.queries.export.OperationExportUseCaseImpl;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationExportUseCaseImplTest {

    @Mock
    private ExportJobTracker jobTracker;

    @Mock
    private IOperationAsyncExportPort asyncExportPort;

    @InjectMocks
    private OperationExportUseCaseImpl useCase;

    @Test
    @DisplayName("execute - debe crear job de exportación de operaciones y lanzar proceso asíncrono")
    void execute_validRequest_shouldCreateJobAndProcess() {
        ExportOperationsRequest request = ExportOperationsRequest.builder()
                .requestedBy("ENT-001")
                .exportFormat(ExportFormat.EXCEL)
                .build();

        when(jobTracker.createJob(eq("ENT-001"), anyString(), eq(ExportFormat.EXCEL), eq(ExportType.OPERATION)))
                .thenReturn("JOB-001");

        String jobId = useCase.execute(request);

        assertEquals("JOB-001", jobId);

        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(jobTracker).createJob(
                eq("ENT-001"),
                fileNameCaptor.capture(),
                eq(ExportFormat.EXCEL),
                eq(ExportType.OPERATION));

        assertTrue(fileNameCaptor.getValue().startsWith("operations_audit_"));
        assertTrue(fileNameCaptor.getValue().endsWith(".xlsx"));

        verify(asyncExportPort).process(request, "JOB-001");
    }
}
