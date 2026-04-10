package com.audit.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.enums.DocumentStatus;

import lombok.Getter;

@Getter
public class AuditDocument {

    // Attributes
    private final Long id;
    private final Long documentId;
    private final String documentNum;
    private final String documentType;
    private final LocalDate documentDate;
    private final Long enterpriseId;
    private final String enterpriseName;
    private final BigDecimal creditTotal;
    private final BigDecimal debitTotal;
    private final String userId;
    private final String userOperation;
    private final Instant operationAt;
    private final DocumentStatus currentStatus;
    private final Long thirdPartyId;
    private final DocumentOperationType operationType;
    private final Instant createdAt;

    // Constructor
    private AuditDocument(Long id, Long documentId, String documentNum, String documentType, LocalDate documentDate,
            Long enterpriseId, String enterpriseName, BigDecimal creditTotal, BigDecimal debitTotal, String userId,
            String userOperation, Instant operationAt, DocumentStatus currentStatus, Long thirdPartyId,
            DocumentOperationType operationType, Instant createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.documentNum = documentNum;
        this.documentType = documentType;
        this.documentDate = documentDate;
        this.enterpriseId = enterpriseId;
        this.enterpriseName = enterpriseName;
        this.creditTotal = creditTotal;
        this.debitTotal = debitTotal;
        this.userId = userId;
        this.userOperation = userOperation;
        this.operationAt = operationAt;
        this.currentStatus = currentStatus;
        this.thirdPartyId = thirdPartyId;
        this.operationType = operationType;
        this.createdAt = createdAt;
    }
}
