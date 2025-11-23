package com.audit.application.usecases.queries;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.GetAuditOperationsQuery;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.AuditOperationFilter;
import com.audit.domain.model.PageResult;
import com.audit.domain.port.output.AuditOperationRepositoryPort;

import java.util.List;

@Service
public class GetAuditOperationsQueryImpl implements GetAuditOperationsQuery {

    private final AuditOperationRepositoryPort auditOperationRepositoryPort;

    public GetAuditOperationsQueryImpl(AuditOperationRepositoryPort auditOperationRepositoryPort) {
        this.auditOperationRepositoryPort = auditOperationRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OperationAuditResponse> execute(GetOperationsRequest request) {
        AuditOperationFilter filter = mapToFilter(request);
        PageResult<AuditOperation> pageResult = auditOperationRepositoryPort.findPageByFilters(filter);
        List<OperationAuditResponse> operations = pageResult.getContent().stream()
                .map(this::toResponse)
                .toList();

        int size = request.getSize() > 0 ? request.getSize() : 20;
        int totalPages = pageResult.getTotalElements() == 0 ? 0
                : (int) Math.ceil((double) pageResult.getTotalElements() / size);
                
        return PageResponse.<OperationAuditResponse>builder()
                .data(operations)
                .totalElements(pageResult.getTotalElements())
                .totalPages(totalPages)
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .hasNext(request.getPage() < totalPages - 1)
                .hasPrevious(request.getPage() > 0)
                .build();
    }

    private AuditOperationFilter mapToFilter(GetOperationsRequest request) {
        return AuditOperationFilter.builder()
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .moduleName(request.getModuleName())
                .affectedTable(request.getAffectedTable())
                .userName(request.getUserName())
                .userRole(request.getUserRole())
                .operationType(request.getOperationType())
                .registerId(request.getRegisterId())
                .enterpriseId(request.getEnterpriseId())
                .page(request.getPage())
                .size(request.getSize())
                .sortField(request.getSortField())
                .sortDirection(request.getSortDirection())
                .requestingUserRole(request.getRequestingUserRole())
                .build();
    }

    private OperationAuditResponse toResponse(AuditOperation operation) {
        return OperationAuditResponse.builder()
                .userName(operation.getUserName())
                .userRole(operation.getUserRole().name())
                .operationType(operation.getOperationType().name())
                .operationAt(operation.getOperationAt())
                .moduleName(operation.getModuleName())
                .affectedTable(operation.getAffectedTable())
                .registerId(operation.getRegisterId())
                .dataObject(operation.getDataObject())
                .build();
    }
}
