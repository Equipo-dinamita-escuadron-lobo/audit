package com.audit.application.port.output;

import java.util.List;

import com.audit.application.internal.query.AuditOperationCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.ModuleTable;

public interface AuditOperationQueryPort {

    PageResult<AuditOperation> findPageByCriteria(AuditOperationCriteria criteria, QueryOptions options);

    long countByCriteria(AuditOperationCriteria criteria);

    List<AuditOperation> findAllForExport(AuditOperationCriteria criteria);

    List<ModuleTable> findDistinctModulesAndTables(String enterpriseId);
}
