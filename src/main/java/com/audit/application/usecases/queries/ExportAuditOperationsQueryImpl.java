package com.audit.application.usecases.queries;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.application.dto.request.ExportOperationsRequest;
import com.audit.application.dto.response.ExportFileResponse;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.internal.ExportConstraints;
import com.audit.application.port.input.queries.ExportAuditOperationsQuery;
import com.audit.application.port.output.FileExportService;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.AuditOperationCriteria;
import com.audit.domain.port.output.AuditOperationRepositoryPort;

@Service
public class ExportAuditOperationsQueryImpl implements ExportAuditOperationsQuery {

    private final AuditOperationRepositoryPort auditOperationRepository;
    private final FileExportService exportService;

    public ExportAuditOperationsQueryImpl(AuditOperationRepositoryPort auditOperationRepository,
            FileExportService exportService) {
        this.auditOperationRepository = auditOperationRepository;
        this.exportService = exportService;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportFileResponse execute(ExportOperationsRequest request) {

        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                request.getDateFrom(),
                request.getDateTo(),
                request.getModuleName(),
                request.getAffectedTable(),
                request.getUserName(),
                request.getUserRole(),
                request.getOperationType(),
                request.getRegisterId(),
                request.getEnterpriseId());

        String format = request.getFormat().toUpperCase();
        Long totalRecords = auditOperationRepository.countByCriteria(criteria);

        ExportConstraints.validateExportable(totalRecords, format);

        List<AuditOperation> operations = auditOperationRepository.findForExport(criteria);

        List<OperationAuditResponse> data = operations.stream()
                .map(this::toResponse)
                .toList();

        byte[] fileContent = exportService.exportOperations(data, format);

        return buildResponse(fileContent, format, data.size());
    }

    public OperationAuditResponse toResponse(AuditOperation operation) {
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

    private ExportFileResponse buildResponse(byte[] fileContent, String format, int recordCount) {
        String timestamp = Instant.now()
                .atZone(ZoneId.of("America/Bogota"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "audit_operations_" + timestamp +  ".xlsx";
        return ExportFileResponse.excel(fileContent, fileName, recordCount);
    }

}
