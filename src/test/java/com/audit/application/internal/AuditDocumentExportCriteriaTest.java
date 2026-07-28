package com.audit.application.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.internal.query.AuditDocumentExportCriteria;
import com.audit.domain.enums.AuditDateType;
import com.audit.domain.enums.DocumentOperationType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class AuditDocumentExportCriteriaTest {

    @Test
    @DisplayName("create - criterio de exportación documental válido")
    void create_validExportCriteria() {
        AuditDocumentExportCriteria criteria = AuditDocumentExportCriteria.create(
                "ENT-001",
                Instant.now().minus(7, ChronoUnit.DAYS),
                Instant.now(),
                AuditDateType.DOCUMENT_DATE,
                "FAC-001",
                "FACTURA",
                "Cliente",
                DocumentOperationType.CREATE,
                "Freider");

        assertAll(
                () -> assertEquals("ENT-001", criteria.getEnterpriseId()),
                () -> assertEquals(AuditDateType.DOCUMENT_DATE, criteria.getDateType()),
                () -> assertTrue(criteria.hasDateRange()),
                () -> assertTrue(criteria.hasDocumentCode()),
                () -> assertTrue(criteria.hasDocumentType()),
                () -> assertTrue(criteria.hasThirdPartyName()),
                () -> assertTrue(criteria.hasOperationType()),
                () -> assertTrue(criteria.hasUserName()));
    }

    @Test
    @DisplayName("create - si dateType es nulo debe usar OPERATION_DATE")
    void create_nullDateType_shouldUseOperationDate() {
        AuditDocumentExportCriteria criteria = AuditDocumentExportCriteria.create(
                "ENT-001",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertEquals(AuditDateType.OPERATION_DATE, criteria.getDateType());
    }

    @Test
    @DisplayName("create - debe fallar si enterpriseId es vacío")
    void create_blankEnterpriseId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> AuditDocumentExportCriteria.create(
                " ",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Test
    @DisplayName("hasCriteria - filtros vacíos deben retornar false")
    void hasCriteria_blankFiltersReturnFalse() {
        AuditDocumentExportCriteria criteria = AuditDocumentExportCriteria.create(
                "ENT-001",
                null,
                null,
                null,
                " ",
                " ",
                " ",
                null,
                " ");

        assertAll(
                () -> assertFalse(criteria.hasDateRange()),
                () -> assertFalse(criteria.hasDocumentCode()),
                () -> assertFalse(criteria.hasDocumentType()),
                () -> assertFalse(criteria.hasThirdPartyName()),
                () -> assertFalse(criteria.hasOperationType()),
                () -> assertFalse(criteria.hasUserName()));
    }
}
