package com.audit.application.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.internal.query.AuditSessionCriteria;
import com.audit.domain.enums.UserAction;
import com.audit.domain.exceptions.InvalidAuditEventException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class AuditSessionCriteriaTest {

    @Test
    @DisplayName("create - criterio válido con filtros")
    void create_validCriteriaWithFilters() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        AuditSessionCriteria criteria = AuditSessionCriteria.create(
                from,
                to,
                "Freider",
                "ADMIN",
                UserAction.LOGIN);

        assertAll(
                () -> assertEquals(from, criteria.getDateFrom()),
                () -> assertEquals(to, criteria.getDateTo()),
                () -> assertTrue(criteria.hasUserNameCriteria()),
                () -> assertTrue(criteria.hasRoleCriteria()),
                () -> assertTrue(criteria.hasActionCriteria()));
    }

    @Test
    @DisplayName("create - debe fallar si dateFrom es nulo")
    void create_nullDateFrom_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> AuditSessionCriteria.create(
                null,
                Instant.now(),
                null,
                null,
                null));
    }

    @Test
    @DisplayName("create - debe fallar si dateFrom es mayor que dateTo")
    void create_dateFromAfterDateTo_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> AuditSessionCriteria.create(
                Instant.now(),
                Instant.now().minus(1, ChronoUnit.DAYS),
                null,
                null,
                null));
    }

    @Test
    @DisplayName("create - debe fallar si las fechas están en el futuro")
    void create_futureDates_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> AuditSessionCriteria.create(
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(2, ChronoUnit.DAYS),
                null,
                null,
                null));
    }

    @Test
    @DisplayName("create - debe fallar si el rango excede 2 años")
    void create_rangeGreaterThanTwoYears_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> AuditSessionCriteria.create(
                Instant.now().minus(800, ChronoUnit.DAYS),
                Instant.now(),
                null,
                null,
                null));
    }

    @Test
    @DisplayName("hasCriteria - filtros vacíos deben retornar false")
    void hasCriteria_blankFiltersReturnFalse() {
        AuditSessionCriteria criteria = AuditSessionCriteria.create(
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now(),
                " ",
                " ",
                null);

        assertAll(
                () -> assertFalse(criteria.hasUserNameCriteria()),
                () -> assertFalse(criteria.hasRoleCriteria()),
                () -> assertFalse(criteria.hasActionCriteria()));
    }
}
