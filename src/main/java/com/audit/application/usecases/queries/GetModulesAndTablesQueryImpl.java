package com.audit.application.usecases.queries;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.application.dto.request.GetModulesTablesRequest;
import com.audit.application.dto.response.ModuleTableResponse;
import com.audit.application.port.input.queries.GetModulesAndTablesQuery;
import com.audit.domain.port.output.AuditOperationRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetModulesAndTablesQueryImpl implements GetModulesAndTablesQuery {

    private final AuditOperationRepositoryPort auditOperationRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<ModuleTableResponse> execute(GetModulesTablesRequest request) {
        return auditOperationRepositoryPort.findDistinctModulesAndTables(request.enterpriseId())
                .stream()
                .map(mt -> new ModuleTableResponse(
                        mt.moduleName(),
                        mt.affectedTable()))
                .toList();
    }

}
