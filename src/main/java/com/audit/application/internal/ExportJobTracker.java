package com.audit.application.internal;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.audit.domain.enums.ExportFormat;
import com.audit.domain.enums.ExportType;
import com.audit.domain.model.ExportJob;

@Service
public class ExportJobTracker {

    private final Map<String, ExportJob> jobs = new ConcurrentHashMap<>();

    public String createJob(String requestedBy, String fileName,
            ExportFormat format, ExportType exportType) {
        ExportJob job = ExportJob.create(requestedBy, fileName, format, exportType);
        jobs.put(job.getJobId(), job);
        return job.getJobId();
    }

    public Optional<ExportJob> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public void removeJob(String jobId) {
        jobs.remove(jobId);
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanStaleJobs() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        jobs.entrySet().removeIf(entry -> {
            ExportJob job = entry.getValue();
            return job.isCompleted() && job.getEndTime().isBefore(threshold);
        });
    }
}
