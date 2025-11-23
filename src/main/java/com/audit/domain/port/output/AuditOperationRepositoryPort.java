package com.audit.domain.port.output;

import java.util.Optional;

import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.AuditOperationFilter;
import com.audit.domain.model.PageResult;

public interface AuditOperationRepositoryPort {

    AuditOperation save(AuditOperation auditOperation);

    PageResult<AuditOperation> findPageByFilters(AuditOperationFilter filter);

    Optional<AuditOperation> findById(Long id);

    Optional<AuditOperation> findByRegisterId(String registerId, String affectedTable);
}
