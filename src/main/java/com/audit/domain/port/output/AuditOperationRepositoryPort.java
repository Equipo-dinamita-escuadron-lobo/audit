package com.audit.domain.port.output;

import java.util.List;
import java.util.Optional;

import com.audit.application.internal.PageResult;
import com.audit.application.internal.QueryOptions;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.AuditOperationCriteria;
import com.audit.domain.model.ModuleTable;

public interface AuditOperationRepositoryPort {

    AuditOperation save(AuditOperation auditOperation);

    Optional<AuditOperation> findById(Long id);

    List<ModuleTable> findDistinctModulesAndTables(String enterpriseId);

    Optional<AuditOperation> findByRegisterId(String registerId, String affectedTable);

    PageResult<AuditOperation> findPageByCriteria(AuditOperationCriteria criteria, QueryOptions options);

    long countByCriteria(AuditOperationCriteria criteria);

    List<AuditOperation> findForExport(AuditOperationCriteria criteria);
}
