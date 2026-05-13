package com.audit.domain.port.output;

import java.util.Optional;

import com.audit.domain.model.AuditOperation;

public interface AuditOperationRepositoryPort {

    AuditOperation save(AuditOperation auditOperation);

    Optional<AuditOperation> findById(Long id);

    Optional<AuditOperation> findByRegisterId(String registerId, String affectedTable);
}
