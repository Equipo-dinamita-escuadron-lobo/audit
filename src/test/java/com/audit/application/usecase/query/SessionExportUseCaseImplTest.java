package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.ExportSessionsRequest;
import com.audit.application.internal.ExportJobTracker;
import com.audit.application.port.output.ISessionAsyncExportPort;
import com.audit.application.usecases.queries.export.SessionExportUseCaseImpl;
import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportType;
import com.audit.domain.enums.UserAction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionExportUseCaseImplTest {

    @Mock
    private ExportJobTracker jobTracker;

    @Mock
    private ISessionAsyncExportPort asyncExportPort;

    @InjectMocks
    private SessionExportUseCaseImpl useCase;

    @Test
    @DisplayName("execute - debe crear job de exportación de sesiones y lanzar proceso asíncrono")
    void execute_validRequest_shouldCreateJobAndProcess() {
        ExportSessionsRequest request = ExportSessionsRequest.builder()
                .requestedBy("ENT-001")
                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                .dateTo(Instant.now())
                .userName("Freider")
                .userRole("ADMIN")
                .action(UserAction.LOGIN)
                .exportFormat(ExportFormat.PDF)
                .build();

        when(jobTracker.createJob(eq("ENT-001"), anyString(), eq(ExportFormat.PDF), eq(ExportType.SESSION)))
                .thenReturn("JOB-001");

        String jobId = useCase.execute(request);

        assertEquals("JOB-001", jobId);

        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(jobTracker).createJob(
                eq("ENT-001"),
                fileNameCaptor.capture(),
                eq(ExportFormat.PDF),
                eq(ExportType.SESSION));

        assertTrue(fileNameCaptor.getValue().startsWith("sessions_"));
        assertTrue(fileNameCaptor.getValue().endsWith(".pdf"));

        verify(asyncExportPort).process(request, "JOB-001");
    }

    @Test
    @DisplayName("execute - formato EXCEL debe generar nombre con extensión xlsx")
    void execute_excelFormat_shouldCreateXlsxFileName() {
        ExportSessionsRequest request = ExportSessionsRequest.builder()
                .requestedBy("ENT-001")
                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                .dateTo(Instant.now())
                .exportFormat(ExportFormat.EXCEL)
                .build();

        when(jobTracker.createJob(eq("ENT-001"), anyString(), eq(ExportFormat.EXCEL), eq(ExportType.SESSION)))
                .thenReturn("JOB-002");

        useCase.execute(request);

        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(jobTracker).createJob(
                eq("ENT-001"),
                fileNameCaptor.capture(),
                eq(ExportFormat.EXCEL),
                eq(ExportType.SESSION));

        assertTrue(fileNameCaptor.getValue().endsWith(".xlsx"));
    }
}
