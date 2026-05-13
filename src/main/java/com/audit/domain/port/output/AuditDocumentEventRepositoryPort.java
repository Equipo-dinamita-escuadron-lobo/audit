package com.audit.domain.port.output;

import java.util.Optional;

import com.audit.domain.model.AuditDocumentEvent;

public interface AuditDocumentEventRepositoryPort {
    AuditDocumentEvent save(AuditDocumentEvent event);

    Optional<AuditDocumentEvent> findById(Long id);
}
