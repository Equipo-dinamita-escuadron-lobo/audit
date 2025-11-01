package com.audit.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.audit.application.port.input.commands.LogAuditSessionCommand;
import com.audit.application.port.input.queries.ExportAuditSessionsQuery;
import com.audit.application.port.input.queries.GetAuditSessionsQuery;
import com.audit.application.port.output.FileExportService;
import com.audit.application.usecases.commands.LogAuditSessionCommandImpl;
import com.audit.application.usecases.queries.ExportAuditSessionsQueryImpl;
import com.audit.application.usecases.queries.GetAuditSessionsQueryImpl;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

@Configuration
public class ApplicationConfig {

    @Bean
    public LogAuditSessionCommand logAuditSessionCommand(
        AuditSessionRepositoryPort auditSessionRepository) 
    {
        return new LogAuditSessionCommandImpl(auditSessionRepository);
    }

    @Bean
    public GetAuditSessionsQuery getAuditSessionsQuery(
        AuditSessionRepositoryPort auditSessionRepository) 
    {
        return new GetAuditSessionsQueryImpl(auditSessionRepository);
    }

    @Bean
    public ExportAuditSessionsQuery exportAuditSessionsQuery(
        AuditSessionRepositoryPort auditSessionRepository, FileExportService fileExportService) 
    {
        return new ExportAuditSessionsQueryImpl(auditSessionRepository, fileExportService);
    }

    
}
