package com.audit.application.usecases.queries;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.response.ExportFileResponse;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.port.input.queries.ExportAuditOperationsQuery;
import com.audit.application.port.output.FileExportService;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.AuditOperationFilter;
import com.audit.domain.port.output.AuditOperationRepositoryPort;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportAuditOperationsQueryImpl implements ExportAuditOperationsQuery {

    private final AuditOperationRepositoryPort auditOperationRepository;
    private final FileExportService exportService;

    public ExportAuditOperationsQueryImpl(AuditOperationRepositoryPort auditOperationRepository, FileExportService exportService) {
        this.auditOperationRepository = auditOperationRepository;
        this.exportService = exportService;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportFileResponse execute(ExportOperationsRequest request) {
        
        AuditOperationFilter filter = buildExportFilter(request);

        List<AuditOperation> operations = auditOperationRepository.findPageByFilters(filter).getContent();

        List<OperationAuditResponse> operationResponses = operations.stream()
                .map(this::toResponse)
                .toList();

        byte[] fileContent = exportService.exportOperations(operationResponses, "EXCEL");
        String fileName = generateFileName(request.getEnterpriseId());

        return ExportFileResponse.excel(fileContent, fileName, operations.size());
    }

    private AuditOperationFilter buildExportFilter(ExportOperationsRequest request) {
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
                .sortField(request.getSortField())
                .sortDirection(request.getSortDirection())
                .requestingUserRole(request.getRequestingUserRole())
                .page(null) 
                .size(null) 
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

    private String generateFileName(String enterpriseId) {
        String timestamp = ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("auditoria_operaciones_empresa_%s_%s.xlsx", enterpriseId, timestamp);
    }
}
