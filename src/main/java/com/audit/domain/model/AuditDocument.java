package com.audit.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.enums.DocumentStatus;

import lombok.Getter;

@Getter
public class AuditDocument {

    // Attributes
    private Long id;
    private Long documentId;
    private String documentNum;
    private String documentType;
    private LocalDate documentDate;
    private Long enterpriseId;
    private String enterpriseName;
    private BigDecimal creditTotal;
    private BigDecimal debitTotal;
    private String userId;
    private String userOperation;
    private ZonedDateTime operationAt;
    private DocumentStatus currentStatus;
    private Long thirdPartyId;
    private DocumentOperationType operationType;
    private ZonedDateTime createdAt;

    // Constructor
}
