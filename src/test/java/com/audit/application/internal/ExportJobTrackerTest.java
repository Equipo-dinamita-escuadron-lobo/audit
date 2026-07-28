package com.audit.application.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportJobStatus;
import com.audit.domain.enums.ExportType;
import com.audit.domain.model.ExportJob;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExportJobTrackerTest {

    private ExportJobTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new ExportJobTracker();
    }

    @Test
    @DisplayName("createJob - debe crear y almacenar job")
    void createJob_shouldStoreJob() {
        String jobId = tracker.createJob(
                "ENT-001",
                "operaciones.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        Optional<ExportJob> job = tracker.getJob(jobId);

        assertAll(
                () -> assertNotNull(jobId),
                () -> assertTrue(job.isPresent()),
                () -> assertEquals("ENT-001", job.get().getEnterpriseId()),
                () -> assertEquals("operaciones.xlsx", job.get().getFileName()),
                () -> assertEquals(ExportFormat.EXCEL, job.get().getFormat()),
                () -> assertEquals(ExportType.OPERATION, job.get().getExportType()),
                () -> assertEquals(ExportJobStatus.PENDING, job.get().getStatus()),
                () -> assertEquals(0, job.get().getProgress()));
    }

    @Test
    @DisplayName("getJob - debe retornar vacío si el job no existe")
    void getJob_notFound_shouldReturnEmpty() {
        Optional<ExportJob> job = tracker.getJob("NO-EXISTE");
        assertTrue(job.isEmpty());
    }

    @Test
    @DisplayName("removeJob - debe eliminar job existente")
    void removeJob_existingJob() {
        String jobId = tracker.createJob(
                "ENT-001",
                "operaciones.xlsx",
                ExportFormat.EXCEL,
                ExportType.OPERATION);

        tracker.removeJob(jobId);
        assertTrue(tracker.getJob(jobId).isEmpty());
    }

    @Test
    @DisplayName("cleanStaleJobs - job completado con 31 minutos debe eliminarse")
    void cleanStaleJobs_completedAt31Minutes_shouldBeRemoved() throws Exception {
        String oldJobId = tracker.createJob("ENT-001", "old.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        ExportJob oldJob = tracker.getJob(oldJobId).get();

        oldJob.markAsProcessing();
        oldJob.complete(new byte[] { 1, 2, 3 }, 100);
        setEndTime(oldJob, LocalDateTime.now().minusMinutes(31));

        invokeCleanStaleJobs(tracker);

        assertTrue(tracker.getJob(oldJobId).isEmpty(), "Job con 31 minutos debe eliminarse");
    }

    @Test
    @DisplayName("removeJob - eliminar job inexistente no debe lanzar excepción")
    void removeJob_nonExistentJob_shouldNotThrowException() {
        assertDoesNotThrow(() -> tracker.removeJob("NO-EXISTE"));
    }

    @Test
    @DisplayName("cleanStaleJobs - debe mantener jobs completados con endTime en el futuro (caso borde)")
    void cleanStaleJobs_completedWithFutureEndTime_shouldRemain() throws Exception {
        String futureJobId = tracker.createJob("ENT-001", "future.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        ExportJob futureJob = tracker.getJob(futureJobId).get();

        futureJob.markAsProcessing();
        futureJob.complete(new byte[] { 1, 2, 3 }, 100);
        setEndTime(futureJob, LocalDateTime.now().plusMinutes(10));

        invokeCleanStaleJobs(tracker);

        assertTrue(tracker.getJob(futureJobId).isPresent(), "Job con endTime futuro debe permanecer");
    }

    @Test
    @DisplayName("getJob - debe retornar job después de múltiples creaciones")
    void getJob_multipleJobs_shouldReturnCorrectJob() {
        String jobId1 = tracker.createJob("ENT-001", "file1.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        String jobId2 = tracker.createJob("ENT-002", "file2.pdf", ExportFormat.PDF, ExportType.OPERATION);

        Optional<ExportJob> job1 = tracker.getJob(jobId1);
        Optional<ExportJob> job2 = tracker.getJob(jobId2);

        assertAll(
                () -> assertTrue(job1.isPresent()),
                () -> assertTrue(job2.isPresent()),
                () -> assertEquals("ENT-001", job1.get().getEnterpriseId()),
                () -> assertEquals("ENT-002", job2.get().getEnterpriseId()),
                () -> assertEquals("file1.xlsx", job1.get().getFileName()),
                () -> assertEquals("file2.pdf", job2.get().getFileName()),
                () -> assertEquals(ExportFormat.EXCEL, job1.get().getFormat()),
                () -> assertEquals(ExportFormat.PDF, job2.get().getFormat()));
    }

    @Test
    @DisplayName("cleanStaleJobs - debe eliminar jobs completados con más de 30 minutos")
    void cleanStaleJobs_shouldRemoveCompletedJobsOlderThan30Minutes() throws Exception {

        String oldJobId = tracker.createJob("ENT-001", "old.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        ExportJob oldJob = tracker.getJob(oldJobId).get();
        oldJob.markAsProcessing();
        oldJob.complete(new byte[] { 1, 2, 3 }, 100);

        setEndTime(oldJob, LocalDateTime.now().minusMinutes(31));

        String recentJobId = tracker.createJob("ENT-002", "recent.xlsx", ExportFormat.PDF, ExportType.OPERATION);
        ExportJob recentJob = tracker.getJob(recentJobId).get();
        recentJob.markAsProcessing();
        recentJob.complete(new byte[] { 4, 5, 6 }, 50);
        String processingJobId = tracker.createJob("ENT-003", "processing.xlsx", ExportFormat.EXCEL,
                ExportType.OPERATION);
        ExportJob processingJob = tracker.getJob(processingJobId).get();
        processingJob.markAsProcessing();

        String pendingJobId = tracker.createJob("ENT-004", "pending.xlsx", ExportFormat.PDF, ExportType.OPERATION);

        invokeCleanStaleJobs(tracker);

        assertAll(
                () -> assertTrue(tracker.getJob(oldJobId).isEmpty(), "Job antiguo debe ser eliminado"),
                () -> assertTrue(tracker.getJob(recentJobId).isPresent(), "Job reciente debe permanecer"),
                () -> assertTrue(tracker.getJob(processingJobId).isPresent(), "Job en proceso debe permanecer"),
                () -> assertTrue(tracker.getJob(pendingJobId).isPresent(), "Job pendiente debe permanecer"));
    }

    @Test
    @DisplayName("cleanStaleJobs - no debe eliminar jobs completados recientes")
    void cleanStaleJobs_shouldNotRemoveRecentlyCompletedJobs() throws Exception {
        String recentJobId = tracker.createJob("ENT-001", "recent.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        ExportJob recentJob = tracker.getJob(recentJobId).get();

        recentJob.markAsProcessing();
        recentJob.complete(new byte[] { 1, 2, 3 }, 100);

        invokeCleanStaleJobs(tracker);

        assertTrue(tracker.getJob(recentJobId).isPresent());
    }

    @Test
    @DisplayName("cleanStaleJobs - no debe eliminar jobs fallidos")
    void cleanStaleJobs_shouldNotRemoveFailedJobs() throws Exception {
        String failedJobId = tracker.createJob("ENT-001", "failed.xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        ExportJob failedJob = tracker.getJob(failedJobId).get();

        failedJob.markAsProcessing();
        failedJob.fail("Error de proceso");

        setEndTime(failedJob, LocalDateTime.now().minusMinutes(31));

        invokeCleanStaleJobs(tracker);

        assertTrue(tracker.getJob(failedJobId).isPresent());
    }

    @Test
    @DisplayName("cleanStaleJobs - jobs sin endTime no deben eliminarse")
    void cleanStaleJobs_shouldNotRemoveJobsWithoutEndTime() throws Exception {
        String processingJobId = tracker.createJob("ENT-001", "processing.xlsx", ExportFormat.EXCEL,
                ExportType.OPERATION);
        ExportJob processingJob = tracker.getJob(processingJobId).get();
        processingJob.markAsProcessing();

        invokeCleanStaleJobs(tracker);

        assertTrue(tracker.getJob(processingJobId).isPresent());
    }

    @Test
    @DisplayName("cleanStaleJobs - mapa vacío no debe lanzar excepción")
    void cleanStaleJobs_emptyMap_shouldNotThrowException() throws Exception {
        assertDoesNotThrow(() -> invokeCleanStaleJobs(tracker));
    }

    @Test
    @DisplayName("cleanStaleJobs - múltiples jobs en diferentes estados")
    void cleanStaleJobs_multipleJobsMixedStates() throws Exception {
        String[] jobIds = new String[5];
        for (int i = 0; i < 5; i++) {
            jobIds[i] = tracker.createJob("ENT-" + i, "file" + i + ".xlsx", ExportFormat.EXCEL, ExportType.OPERATION);
        }

        ExportJob job0 = tracker.getJob(jobIds[0]).get();
        job0.markAsProcessing();
        job0.complete(new byte[] { 1 }, 10);
        setEndTime(job0, LocalDateTime.now().minusMinutes(31));

        ExportJob job1 = tracker.getJob(jobIds[1]).get();
        job1.markAsProcessing();
        job1.complete(new byte[] { 2 }, 20);

        ExportJob job2 = tracker.getJob(jobIds[2]).get();
        job2.markAsProcessing();
        job2.fail("Error");
        setEndTime(job2, LocalDateTime.now().minusMinutes(31));

        ExportJob job3 = tracker.getJob(jobIds[3]).get();
        job3.markAsProcessing();

        invokeCleanStaleJobs(tracker);

        assertAll(
                () -> assertTrue(tracker.getJob(jobIds[0]).isEmpty(), "Job 0 debe eliminarse"),
                () -> assertTrue(tracker.getJob(jobIds[1]).isPresent(), "Job 1 debe permanecer"),
                () -> assertTrue(tracker.getJob(jobIds[2]).isPresent(), "Job 2 debe permanecer"),
                () -> assertTrue(tracker.getJob(jobIds[3]).isPresent(), "Job 3 debe permanecer"),
                () -> assertTrue(tracker.getJob(jobIds[4]).isPresent(), "Job 4 debe permanecer"));
    }

    private void setEndTime(ExportJob job, LocalDateTime endTime) throws Exception {
        java.lang.reflect.Field endTimeField = ExportJob.class.getDeclaredField("endTime");
        endTimeField.setAccessible(true);
        endTimeField.set(job, endTime);
    }

    private void invokeCleanStaleJobs(ExportJobTracker tracker) throws Exception {
        Method cleanMethod = ExportJobTracker.class.getDeclaredMethod("cleanStaleJobs");
        cleanMethod.setAccessible(true);
        cleanMethod.invoke(tracker);
    }
}
