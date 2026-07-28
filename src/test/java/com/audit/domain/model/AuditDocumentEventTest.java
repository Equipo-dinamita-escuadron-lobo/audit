package com.audit.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.exceptions.InvalidAuditEventException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditDocumentEventTest {

        private DocumentData validDocumentData() {
                return DocumentData.of(
                                Map.of("documentCode", "FAC-001"),
                                List.of(Map.of("account", "1105", "debit", 1000)),
                                Map.of("total", 1000),
                                Map.of("source", "sales"));
        }

        @Test
        @DisplayName("create - evento documental válido debe crearse")
        void create_validDocumentEvent() {
                AuditDocumentEvent event = AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                validDocumentData());

                assertAll(
                                () -> assertNull(event.getId()),
                                () -> assertEquals("ENT-001", event.getEnterpriseId()),
                                () -> assertEquals("FAC-001", event.getDocumentCode()),
                                () -> assertEquals("FACTURA", event.getDocumentType()),
                                () -> assertEquals("DOC-001", event.getDocumentId()),
                                () -> assertEquals(DocumentOperationType.CREATE, event.getOperationType()),
                                () -> assertNotNull(event.getCreatedAt()));
        }

        @Test
        @DisplayName("create - debe fallar si documentId es vacío")
        void create_emptyDocumentId_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                " ",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                validDocumentData()));
        }

        @Test
        @DisplayName("create - debe fallar si documentDate es nula")
        void create_nullDocumentDate_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                null,
                                validDocumentData()));
        }

        @Test
        @DisplayName("create - debe fallar si thirdPartyId viene vacío")
        void create_blankThirdPartyId_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                " ",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                validDocumentData()));
        }

        @Test
        @DisplayName("create - debe fallar si operationAt está en el futuro")
        void create_futureOperationAt_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now().plusSeconds(301),
                                LocalDate.now(),
                                validDocumentData()));
        }

        @Test
        @DisplayName("reconstruct - debe usar lista vacía cuando userRoles es null")
        void reconstruct_nullRoles_returnsEmptyList() {

                AuditDocumentEvent event = AuditDocumentEvent.reconstruct(
                                1L,
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                null,
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                validDocumentData(),
                                Instant.now());

                assertNotNull(event.getUserRoles());
                assertTrue(event.getUserRoles().isEmpty());
        }

        @Test
        @DisplayName("reconstruct - debe reconstruir un evento existente")
        void reconstruct_existingEvent() {

                Instant createdAt = Instant.now().minusSeconds(3600);
                Instant operationAt = Instant.now();

                AuditDocumentEvent event = AuditDocumentEvent.reconstruct(
                                10L,
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.UPDATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                operationAt,
                                LocalDate.now(),
                                validDocumentData(),
                                createdAt);

                assertAll(
                                () -> assertEquals(10L, event.getId()),
                                () -> assertEquals("ENT-001", event.getEnterpriseId()),
                                () -> assertEquals(DocumentOperationType.UPDATE, event.getOperationType()),
                                () -> assertEquals(createdAt, event.getCreatedAt()),
                                () -> assertEquals(operationAt, event.getOperationAt()));
        }

        @Test
        @DisplayName("create - debe fallar si enterpriseId es nulo")
        void create_nullEnterpriseId_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                null,
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                validDocumentData()));
        }

        @Test
        @DisplayName("create - debe fallar si operationType es nulo")
        void create_nullOperationType_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                null,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                validDocumentData()));
        }

        @Test
        @DisplayName("create - debe fallar si userRoles está vacío")
        void create_emptyRoles_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of(),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                validDocumentData()));
        }

        @Test
        @DisplayName("create - debe fallar si documentData es nulo")
        void create_nullDocumentData_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                Instant.now(),
                                LocalDate.now(),
                                null));
        }

        @Test
        @DisplayName("create - debe fallar si thirdPartyName viene vacío")
        void create_blankThirdPartyName_throws() {
                assertThrows(InvalidAuditEventException.class,
                                () -> AuditDocumentEvent.create("ENT-001", "FAC-001", "FACTURA", "DOC-001",
                                                "USER-001", "Freider",
                                                List.of("ADMIN"),
                                                DocumentOperationType.CREATE,
                                                "TP-001", " ",
                                                "MODULE",
                                                Instant.now(), LocalDate.now(), validDocumentData()));
        }

        @Test
        @DisplayName("create - debe fallar si moduleName viene vacío")
        void create_blankModuleName_throws() {
                assertThrows(InvalidAuditEventException.class,
                                () -> AuditDocumentEvent.create("ENT-001", "FAC-001", "FACTURA", "DOC-001",
                                                "USER-001", "Freider",
                                                List.of("ADMIN"),
                                                DocumentOperationType.CREATE,
                                                "TP-001", "Cliente", " ",
                                                Instant.now(), LocalDate.now(), validDocumentData()));
        }

        @Test
        @DisplayName("create - thirdPartyId nulo debe pasar sin excepción")
        void create_nullThirdPartyId_passes() {
                AuditDocumentEvent event = AuditDocumentEvent.create("ENT-001", "FAC-001", "FACTURA", "DOC-001",
                                "USER-001", "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                null, "Cliente",
                                "MODULE",
                                Instant.now(), LocalDate.now(), validDocumentData());
                assertNotNull(event);
        }

        @Test
        @DisplayName("create - debe fallar si operationAt es nulo")
        void create_nullOperationAt_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditDocumentEvent.create(
                                "ENT-001",
                                "FAC-001",
                                "FACTURA",
                                "DOC-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                DocumentOperationType.CREATE,
                                "TP-001",
                                "Cliente prueba",
                                "DOCUMENTS",
                                null,
                                LocalDate.now(),
                                validDocumentData()));
        }
}
