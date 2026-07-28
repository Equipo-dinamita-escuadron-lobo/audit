package com.audit.infrastructure.adpaters.input.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.infrastructure.adapters.input.rest.dto.request.DateRangeRequest;
import com.audit.infrastructure.adapters.input.rest.validation.DateRangeValidator;

import jakarta.validation.ConstraintValidatorContext;

class DateRangeValidatorTest {

    private final DateRangeValidator validator = new DateRangeValidator();
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    private DateRangeRequest request(ZonedDateTime from, ZonedDateTime to) {
        return new DateRangeRequest() {
            public ZonedDateTime getDateFrom() {
                return from;
            }

            public ZonedDateTime getDateTo() {
                return to;
            }
        };
    }

    @Test
    @DisplayName("isValid - ambas fechas null debe retornar true")
    void isValid_bothNull_shouldReturnTrue() {
        assertTrue(validator.isValid(request(null, null), context));
    }

    @Test
    @DisplayName("isValid - dateFrom null debe retornar true")
    void isValid_dateFromNull_shouldReturnTrue() {
        assertTrue(validator.isValid(request(null, ZonedDateTime.now()), context));
    }

    @Test
    @DisplayName("isValid - dateTo null debe retornar true")
    void isValid_dateToNull_shouldReturnTrue() {
        assertTrue(validator.isValid(request(ZonedDateTime.now(), null), context));
    }

    @Test
    @DisplayName("isValid - rango válido de 30 días debe retornar true")
    void isValid_validRange_shouldReturnTrue() {
        ZonedDateTime from = ZonedDateTime.now().minusDays(30);
        ZonedDateTime to = ZonedDateTime.now();
        assertTrue(validator.isValid(request(from, to), context));
    }

    @Test
    @DisplayName("isValid - dateFrom igual a dateTo debe retornar false")
    void isValid_dateFromEqualsDateTo_shouldReturnFalse() {
        ZonedDateTime now = ZonedDateTime.now();
        assertFalse(validator.isValid(request(now, now), context));
        verify(context).buildConstraintViolationWithTemplate("dateFrom must be before dateTo");
    }

    @Test
    @DisplayName("isValid - dateFrom después de dateTo debe retornar false")
    void isValid_dateFromAfterDateTo_shouldReturnFalse() {
        ZonedDateTime from = ZonedDateTime.now();
        ZonedDateTime to = ZonedDateTime.now().minusDays(1);
        assertFalse(validator.isValid(request(from, to), context));
        verify(context).buildConstraintViolationWithTemplate("dateFrom must be before dateTo");
    }

    @Test
    @DisplayName("isValid - rango exacto de 365 días debe retornar true")
    void isValid_exactly365Days_shouldReturnTrue() {
        ZonedDateTime from = ZonedDateTime.now().minusDays(365);
        ZonedDateTime to = ZonedDateTime.now();
        assertTrue(validator.isValid(request(from, to), context));
    }

    @Test
    @DisplayName("isValid - rango de 366 días debe retornar false")
    void isValid_366Days_shouldReturnFalse() {
        ZonedDateTime from = ZonedDateTime.now().minusDays(366);
        ZonedDateTime to = ZonedDateTime.now();
        assertFalse(validator.isValid(request(from, to), context));
        verify(context).buildConstraintViolationWithTemplate("Date range cannot exceed 365 days");
    }
}
