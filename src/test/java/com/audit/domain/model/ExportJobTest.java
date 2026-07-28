package com.audit.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportJobStatus;
import com.audit.domain.enums.ExportType;
import com.audit.domain.exceptions.ExportAuditException;

import static org.junit.jupiter.api.Assertions.*;

class ExportJobTest {

    @Test
    @DisplayName("create - debe crear job pendiente")
    void create_pendingJob() {
        ExportJob job = ExportJob.create(
                "ENT-001",
                "auditoria.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        assertAll(
                () -> assertNotNull(job.getJobId()),
                () -> assertEquals("ENT-001", job.getEnterpriseId()),
                () -> assertEquals("auditoria.xlsx", job.getFileName()),
                () -> assertEquals(ExportJobStatus.PENDING, job.getStatus()),
                () -> assertEquals(0, job.getProgress()),
                () -> assertEquals(0, job.getTotalRecords()),
                () -> assertNotNull(job.getStartTime()),
                () -> assertNull(job.getEndTime()));
    }

    @Test
    @DisplayName("markAsProcessing - job pendiente debe pasar a procesamiento")
    void markAsProcessing_pendingJob() {
        ExportJob job = ExportJob.create("ENT-001", "auditoria.pdf", ExportFormat.PDF, ExportType.SESSION);

        job.markAsProcessing();

        assertAll(
                () -> assertEquals(ExportJobStatus.PROCESSING, job.getStatus()),
                () -> assertEquals(10, job.getProgress()));
    }

    @Test
    @DisplayName("updateProgress - progreso válido debe actualizarse")
    void updateProgress_validProgress() {
        ExportJob job = ExportJob.create("ENT-001", "auditoria.pdf", ExportFormat.PDF, ExportType.SESSION);

        job.updateProgress(50);

        assertEquals(50, job.getProgress());
    }

    @Test
    @DisplayName("updateProgress - progreso inválido debe fallar")
    void updateProgress_invalidProgress_throwsException() {
        ExportJob job = ExportJob.create("ENT-001", "auditoria.pdf", ExportFormat.PDF, ExportType.SESSION);

        assertAll(
                () -> assertThrows(ExportAuditException.class, () -> job.updateProgress(-1)),
                () -> assertThrows(ExportAuditException.class, () -> job.updateProgress(101)));
    }

    @Test
    @DisplayName("complete - job en procesamiento debe completarse")
    void complete_processingJob() {
        ExportJob job = ExportJob.create("ENT-001", "auditoria.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        byte[] fileData = "archivo".getBytes();

        job.markAsProcessing();
        job.complete(fileData, 5);

        assertAll(
                () -> assertEquals(ExportJobStatus.COMPLETED, job.getStatus()),
                () -> assertEquals(100, job.getProgress()),
                () -> assertEquals(5, job.getTotalRecords()),
                () -> assertArrayEquals(fileData, job.getFileData()),
                () -> assertTrue(job.isCompleted()),
                () -> assertFalse(job.isFailed()),
                () -> assertNotNull(job.getEndTime()));
    }

    @Test
    @DisplayName("complete - job pendiente no puede completarse")
    void complete_pendingJob_throwsException() {
        ExportJob job = ExportJob.create("ENT-001", "auditoria.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);

        assertThrows(ExportAuditException.class, () -> job.complete("archivo".getBytes(), 5));
    }

    @Test
    @DisplayName("fail - job en procesamiento debe marcarse como fallido")
    void fail_processingJob() {
        ExportJob job = ExportJob.create("ENT-001", "auditoria.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);

        job.markAsProcessing();
        job.fail("Error generando archivo");

        assertAll(
                () -> assertEquals(ExportJobStatus.FAILED, job.getStatus()),
                () -> assertEquals("Error generando archivo", job.getErrorMessage()),
                () -> assertTrue(job.isFailed()),
                () -> assertFalse(job.isCompleted()),
                () -> assertNotNull(job.getEndTime()));
    }

    @Test
    @DisplayName("fail - job pendiente no puede fallar")
    void fail_pendingJob_throwsException() {
        ExportJob job = ExportJob.create("ENT-001", "auditoria.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);

        assertThrows(ExportAuditException.class, () -> job.fail("Error"));
    }
}
