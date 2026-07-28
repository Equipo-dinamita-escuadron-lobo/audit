package com.audit.application.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.internal.query.AuditOperationCriteria;
import com.audit.domain.enums.OperationType;
import com.audit.domain.exceptions.InvalidAuditEventException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class AuditOperationCriteriaTest {

    @Test
    @DisplayName("create - criterio válido con todos los filtros")
    void create_validCriteriaWithFilters() {
        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                Instant.now().minus(7, ChronoUnit.DAYS),
                Instant.now(),
                "CONFIGURATION",
                "cost_centers",
                "Freider",
                "ADMIN",
                OperationType.UPDATE,
                "1",
                "ENT-001");

        assertAll(
                () -> assertTrue(criteria.hasModuleNameCriteria()),
                () -> assertTrue(criteria.hasAffectedTableCriteria()),
                () -> assertTrue(criteria.hasUserNameCriteria()),
                () -> assertTrue(criteria.hasUserRoleCriteria()),
                () -> assertTrue(criteria.hasOperationTypeCriteria()),
                () -> assertTrue(criteria.hasRegisterIdCriteria()),
                () -> assertTrue(criteria.hasEnterpriseIdCriteria()));
    }

    @Test
    @DisplayName("create - debe fallar si falta rango de fechas")
    void create_missingDateRange_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> AuditOperationCriteria.create(
                null,
                Instant.now(),
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Test
    @DisplayName("create - debe fallar si dateFrom es mayor que dateTo")
    void create_dateFromAfterDateTo_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> AuditOperationCriteria.create(
                Instant.now(),
                Instant.now().minus(1, ChronoUnit.DAYS),
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Test
    @DisplayName("create - debe fallar si el rango supera 2 años")
    void create_rangeGreaterThanTwoYears_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> AuditOperationCriteria.create(
                Instant.now().minus(800, ChronoUnit.DAYS),
                Instant.now(),
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
        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now(),
                " ",
                " ",
                " ",
                " ",
                null,
                " ",
                " ");

        assertAll(
                () -> assertFalse(criteria.hasModuleNameCriteria()),
                () -> assertFalse(criteria.hasAffectedTableCriteria()),
                () -> assertFalse(criteria.hasUserNameCriteria()),
                () -> assertFalse(criteria.hasUserRoleCriteria()),
                () -> assertFalse(criteria.hasOperationTypeCriteria()),
                () -> assertFalse(criteria.hasRegisterIdCriteria()),
                () -> assertFalse(criteria.hasEnterpriseIdCriteria()));
    }
}
