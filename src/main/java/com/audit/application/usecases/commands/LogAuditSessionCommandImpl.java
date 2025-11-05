package com.audit.application.usecases.commands;

import com.audit.application.dto.request.LogSessionRequest;
import com.audit.application.port.input.commands.LogAuditSessionCommand;
import com.audit.domain.model.AuditSession;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

/**
 * @brief Use case implementation for logging audit sessions (login/logoutevents)
 * 
 */

public class LogAuditSessionCommandImpl implements LogAuditSessionCommand {
    
    private final AuditSessionRepositoryPort auditSessionRepository;

    public LogAuditSessionCommandImpl(AuditSessionRepositoryPort auditSessionRepository) {
        this.auditSessionRepository = auditSessionRepository;
    }

    @Override
    public void executeAsync(LogSessionRequest request) {

        try {
            AuditSession session = AuditSession.create(
                    request.getSessionId(),
                    request.getUserId(),
                    request.getUserName(),
                    request.getUserRole(),
                    request.getAction(),
                    request.getActionAt(),
                    request.getIpAddress());

            auditSessionRepository.save(session);
        } catch (Exception e) {
            throw e;
        }

    }
}
