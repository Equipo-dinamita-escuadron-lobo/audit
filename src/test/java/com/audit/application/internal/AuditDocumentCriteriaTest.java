package com.audit.application.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.internal.query.AuditDocumentCriteria;
import com.audit.domain.enums.AuditDateType;
import com.audit.domain.exceptions.InvalidAuditCriteriaException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class AuditDocumentCriteriaTest {

    @Test
    @DisplayName("create - criterio documental válido debe usar dateType recibido")
    void create_validCriteriaWithDateType() {
        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                Instant.now().minus(7, ChronoUnit.DAYS),
                Instant.now(),
                AuditDateType.DOCUMENT_DATE,
                "FACTURA",
                "FAC-001",
                "ENT-001",
                "Cliente",
                "Freider");

        assertAll(
                () -> assertEquals(AuditDateType.DOCUMENT_DATE, criteria.getDateType()),
                () -> assertTrue(criteria.hasDateRange()),
                () -> assertTrue(criteria.hasDocumentType()),
                () -> assertTrue(criteria.hasDocumentCode()),
                () -> assertTrue(criteria.hasEnterpriseId()),
                () -> assertTrue(criteria.hasThirdPartyName()),
                () -> assertTrue(criteria.hasCreatedBy()));
    }

    @Test
    @DisplayName("create - si dateType es nulo debe usar OPERATION_DATE")
    void create_nullDateType_shouldUseOperationDate() {
        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
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
    @DisplayName("create - debe fallar si dateFrom es mayor que dateTo")
    void create_dateFromAfterDateTo_throwsException() {
        assertThrows(InvalidAuditCriteriaException.class, () -> AuditDocumentCriteria.create(
                Instant.now(),
                Instant.now().minus(1, ChronoUnit.DAYS),
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
        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                null,
                null,
                null,
                " ",
                " ",
                " ",
                " ",
                " ");

        assertAll(
                () -> assertFalse(criteria.hasDateRange()),
                () -> assertFalse(criteria.hasDocumentType()),
                () -> assertFalse(criteria.hasDocumentCode()),
                () -> assertFalse(criteria.hasEnterpriseId()),
                () -> assertFalse(criteria.hasThirdPartyName()),
                () -> assertFalse(criteria.hasCreatedBy()));
    }
}
