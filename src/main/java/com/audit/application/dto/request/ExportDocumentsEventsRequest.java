package com.audit.application.dto.request;

import java.time.Instant;

import com.audit.domain.enums.AuditDateType;
import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.enums.ExportFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportDocumentsEventsRequest {
    private String enterpriseId;
    private String enterpriseName;
    private Instant dateFrom;
    private Instant dateTo;
    private AuditDateType dateType;
    private String documentCode;
    private String documentType;
    private String thirdPartyName;
    private DocumentOperationType operationType;
    private String userName;
    private String requestedBy;
    @Builder.Default
    private ExportFormat exportFormat = ExportFormat.EXCEL;
}
