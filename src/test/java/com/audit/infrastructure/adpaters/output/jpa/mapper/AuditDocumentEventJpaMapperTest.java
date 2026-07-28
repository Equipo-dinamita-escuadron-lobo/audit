package com.audit.infrastructure.adpaters.output.jpa.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.model.AuditDocumentEvent;
import com.audit.domain.model.DocumentData;
import com.audit.infrastructure.adapters.output.exception.security.AuditMappingException;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditDocumentEventEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditDocumentEventJpaMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditDocumentEventJpaMapperTest {

    private final AuditDocumentEventJpaMapper mapper = new AuditDocumentEventJpaMapper();

    private DocumentData documentData() {
        return DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                List.of(Map.of("account", "1105", "debit", 1000)),
                Map.of("total", 1000),
                Map.of("source", "sales"));
    }

    @Test
    @DisplayName("toEntity - debe serializar DocumentData")
    void toEntity_validDomain_shouldSerializeDocumentData() {
        Instant now = Instant.now();
        LocalDate documentDate = LocalDate.now();

        AuditDocumentEvent domain = AuditDocumentEvent.reconstruct(
                1L,
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
                now,
                documentDate,
                documentData(),
                now);

        AuditDocumentEventEntity entity = mapper.toEntity(domain);

        assertAll(
                () -> assertEquals(1L, entity.getId()),
                () -> assertEquals("ENT-001", entity.getEnterpriseId()),
                () -> assertEquals("FAC-001", entity.getDocumentCode()),
                () -> assertTrue(entity.getDocumentData().containsKey("header")),
                () -> assertTrue(entity.getDocumentData().containsKey("details")),
                () -> assertTrue(entity.getDocumentData().containsKey("totals")),
                () -> assertTrue(entity.getDocumentData().containsKey("metadata")));
    }

    @Test
    @DisplayName("toDomain - debe reconstruir DocumentData desde entidad")
    void toDomain_validEntity_shouldParseDocumentData() {
        Instant now = Instant.now();
        LocalDate documentDate = LocalDate.now();

        AuditDocumentEventEntity entity = AuditDocumentEventEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .documentCode("FAC-001")
                .documentType("FACTURA")
                .documentId("DOC-001")
                .userId("USER-001")
                .userName("Freider")
                .userRoles(List.of("ADMIN"))
                .operationType(DocumentOperationType.CREATE)
                .thirdPartyId("TP-001")
                .thirdPartyName("Cliente prueba")
                .moduleName("DOCUMENTS")
                .operationAt(now)
                .documentDate(documentDate)
                .documentData(Map.of(
                        "header", Map.of("documentCode", "FAC-001"),
                        "details", List.of(Map.of("account", "1105")),
                        "totals", Map.of("total", 1000),
                        "metadata", Map.of("source", "sales")))
                .createdAt(now)
                .build();

        AuditDocumentEvent domain = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(1L, domain.getId()),
                () -> assertEquals("FAC-001", domain.getDocumentCode()),
                () -> assertEquals("FACTURA", domain.getDocumentType()),
                () -> assertEquals("FAC-001", domain.getDocumentData().getHeader().get("documentCode")),
                () -> assertEquals(1, domain.getDocumentData().getDetails().size()));
    }

    @Test
    @DisplayName("toDomain - debe fallar si documentData está vacío")
    void toDomain_emptyDocumentData_shouldThrowException() {
        AuditDocumentEventEntity entity = AuditDocumentEventEntity.builder()
                .documentData(Map.of())
                .build();

        assertThrows(AuditMappingException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toEntity - debe fallar si DocumentData es nulo")
    void toEntity_nullDocumentData_shouldThrowException() {
        AuditDocumentEvent domain = AuditDocumentEvent.reconstruct(
                1L,
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
                null,
                Instant.now());

        assertThrows(AuditMappingException.class, () -> mapper.toEntity(domain));
    }
}
