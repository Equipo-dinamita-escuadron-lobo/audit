package com.audit.application.usecases.commands;

import org.springframework.stereotype.Service;

import com.audit.application.dto.request.LogDocumentEventRequest;
import com.audit.application.port.input.commands.LogAuditDocumentEventCommand;
import com.audit.domain.model.AuditDocumentEvent;
import com.audit.domain.port.output.AuditDocumentEventRepositoryPort;

import org.springframework.transaction.annotation.Transactional;

@Service
public class LogAuditDocumentEventCommandImpl implements LogAuditDocumentEventCommand {

    private final AuditDocumentEventRepositoryPort auditDocumentEventRepository;

    public LogAuditDocumentEventCommandImpl(AuditDocumentEventRepositoryPort auditDocumentEventRepository) {
        this.auditDocumentEventRepository = auditDocumentEventRepository;
    }

    @Override
    @Transactional
    public void execute(LogDocumentEventRequest request) {
        AuditDocumentEvent documentEvent = AuditDocumentEvent.create(
                request.getEnterpriseId(),
                request.getDocumentCode(),
                request.getDocumentType(),
                request.getDocumentId(),
                request.getUserId(),
                request.getUserName(),
                request.getUserRoles(),
                request.getOperationType(),
                request.getThirdPartyId(),
                request.getThirdPartyName(),
                request.getModuleName(),
                request.getOperationAt(),
                request.getDocumentDate(),
                request.getDocumentData());
        auditDocumentEventRepository.save(documentEvent);
    }

}
