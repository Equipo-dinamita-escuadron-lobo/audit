package com.audit.application.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.exceptions.InvalidAuditEventException;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DocumentSummaryProjectionTest {

    @Test
    @DisplayName("of - proyección documental válida")
    void of_validProjection() {
        Instant modifiedAt = Instant.now();

        DocumentSummaryProjection projection = DocumentSummaryProjection.of(
                "FAC-001",
                "FACTURA",
                "Cliente prueba",
                LocalDate.now(),
                "Freider",
                "Admin",
                modifiedAt);

        assertAll(
                () -> assertEquals("FAC-001", projection.getDocumentCode()),
                () -> assertEquals("FACTURA", projection.getDocumentType()),
                () -> assertEquals("Cliente prueba", projection.getThirdPartyName()),
                () -> assertEquals("Freider", projection.getCreatedBy()),
                () -> assertEquals("Admin", projection.getLastModifiedBy()),
                () -> assertEquals(modifiedAt, projection.getLastModifiedAt()));
    }

    @Test
    @DisplayName("of - debe fallar si documentCode es vacío")
    void of_blankDocumentCode_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> DocumentSummaryProjection.of(
                " ",
                "FACTURA",
                "Cliente prueba",
                LocalDate.now(),
                "Freider",
                "Admin",
                Instant.now()));
    }

    @Test
    @DisplayName("of - debe fallar si documentType es vacío")
    void of_blankDocumentType_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> DocumentSummaryProjection.of(
                "FAC-001",
                " ",
                "Cliente prueba",
                LocalDate.now(),
                "Freider",
                "Admin",
                Instant.now()));
    }

    @Test
    @DisplayName("of - debe fallar si documentDate es nula")
    void of_nullDocumentDate_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> DocumentSummaryProjection.of(
                "FAC-001",
                "FACTURA",
                "Cliente prueba",
                null,
                "Freider",
                "Admin",
                Instant.now()));
    }

    @Test
    @DisplayName("of - debe fallar si createdBy es vacío")
    void of_blankCreatedBy_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> DocumentSummaryProjection.of(
                "FAC-001",
                "FACTURA",
                "Cliente prueba",
                LocalDate.now(),
                " ",
                "Admin",
                Instant.now()));
    }

    @Test
    @DisplayName("of - debe fallar si lastModifiedAt es nulo")
    void of_nullLastModifiedAt_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> DocumentSummaryProjection.of(
                "FAC-001",
                "FACTURA",
                "Cliente prueba",
                LocalDate.now(),
                "Freider",
                "Admin",
                null));
    }
}
