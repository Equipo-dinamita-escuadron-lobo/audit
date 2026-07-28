package com.audit.infrastructure.adpaters.output.jpa.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.OperationType;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.OperationData;
import com.audit.infrastructure.adapters.output.exception.security.AuditMappingException;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditOperationEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditOperationJpaMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditOperationJpaMapperTest {

    private final AuditOperationJpaMapper mapper = new AuditOperationJpaMapper();

    @Test
    @DisplayName("toEntity - CREATE debe serializar entity dentro de dataObject")
    void toEntity_createOperation_shouldSerializeEntity() {
        OperationData data = OperationData.forCreate(Map.of("id", 1L, "name", "Centro"));

        AuditOperation domain = AuditOperation.reconstruct(
                1L,
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                OperationType.CREATE,
                Instant.now(),
                "CONFIGURATION",
                "cost_centers",
                "1",
                "ENT-001",
                data,
                Instant.now());

        AuditOperationEntity entity = mapper.toEntity(domain);

        assertAll(
                () -> assertEquals(1L, entity.getId()),
                () -> assertEquals("ENT-001", entity.getEnterpriseId()),
                () -> assertTrue(entity.getDataObject().containsKey("entity")),
                () -> assertEquals("Centro", ((Map<?, ?>) entity.getDataObject().get("entity")).get("name")));
    }

    @Test
    @DisplayName("toEntity - UPDATE debe serializar changes")
    void toEntity_updateOperation_shouldSerializeChanges() {
        OperationData data = OperationData.forUpdate(Map.of(
                "name", OperationData.FieldChange.of("Anterior", "Nuevo")));

        AuditOperation domain = AuditOperation.reconstruct(
                1L,
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                OperationType.UPDATE,
                Instant.now(),
                "CONFIGURATION",
                "cost_centers",
                "1",
                "ENT-001",
                data,
                Instant.now());

        AuditOperationEntity entity = mapper.toEntity(domain);

        Map<?, ?> changes = (Map<?, ?>) entity.getDataObject().get("changes");
        Map<?, ?> nameChange = (Map<?, ?>) changes.get("name");

        assertAll(
                () -> assertTrue(entity.getDataObject().containsKey("changes")),
                () -> assertEquals("Anterior", nameChange.get("before")),
                () -> assertEquals("Nuevo", nameChange.get("after")));
    }

    @Test
    @DisplayName("toEntity - UPDATE con contexto debe serializar context y changes")
    void toEntity_updateWithContext_shouldSerializeContextAndChanges() {
        OperationData data = OperationData.forUpdate(
                Map.of("ip", "127.0.0.1"),
                Map.of("status", OperationData.FieldChange.of(false, true)));

        AuditOperation domain = AuditOperation.reconstruct(
                1L,
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                OperationType.UPDATE,
                Instant.now(),
                "CONFIGURATION",
                "cost_centers",
                "1",
                "ENT-001",
                data,
                Instant.now());

        AuditOperationEntity entity = mapper.toEntity(domain);

        assertAll(
                () -> assertTrue(entity.getDataObject().containsKey("context")),
                () -> assertTrue(entity.getDataObject().containsKey("changes")),
                () -> assertEquals("127.0.0.1", ((Map<?, ?>) entity.getDataObject().get("context")).get("ip")));
    }

    @Test
    @DisplayName("toDomain - CREATE debe reconstruir OperationData desde entity")
    void toDomain_createEntity_shouldParseEntityData() {
        Instant now = Instant.now();

        AuditOperationEntity entity = AuditOperationEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.CREATE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .dataObject(Map.of("entity", Map.of("id", 1L, "name", "Centro")))
                .createdAt(now)
                .build();

        AuditOperation domain = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(1L, domain.getId()),
                () -> assertEquals(OperationType.CREATE, domain.getOperationType()),
                () -> assertEquals("Centro", domain.getDataObject().getEntity().get("name")),
                () -> assertTrue(domain.getDataObject().getChanges().isEmpty()));
    }

    @Test
    @DisplayName("toDomain - UPDATE debe reconstruir cambios")
    void toDomain_updateEntity_shouldParseChanges() {
        Instant now = Instant.now();

        AuditOperationEntity entity = AuditOperationEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.UPDATE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .dataObject(Map.of(
                        "changes", Map.of(
                                "name", Map.of("before", "Anterior", "after", "Nuevo"))))
                .createdAt(now)
                .build();

        AuditOperation domain = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(OperationType.UPDATE, domain.getOperationType()),
                () -> assertEquals("Anterior", domain.getDataObject().getChanges().get("name").getBefore()),
                () -> assertEquals("Nuevo", domain.getDataObject().getChanges().get("name").getAfter()));
    }

    @Test
    @DisplayName("toDomain - debe fallar si dataObject está vacío")
    void toDomain_emptyDataObject_shouldThrowException() {
        AuditOperationEntity entity = AuditOperationEntity.builder()
                .operationType(OperationType.CREATE)
                .dataObject(Map.of())
                .build();

        assertThrows(AuditMappingException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toDomain - UPDATE sin changes debe fallar")
    void toDomain_updateWithoutChanges_shouldThrowException() {
        AuditOperationEntity entity = AuditOperationEntity.builder()
                .operationType(OperationType.UPDATE)
                .dataObject(Map.of("entity", Map.of("id", 1L)))
                .build();

        assertThrows(AuditMappingException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toDomain - dataObject nulo debe fallar")
    void toDomain_nullDataObject_shouldThrowException() {
        AuditOperationEntity entity = AuditOperationEntity.builder()
                .operationType(OperationType.CREATE)
                .dataObject(null)
                .build();

        assertThrows(AuditMappingException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toDomain - CREATE con dataObject plano (sin entity) debe funcionar")
    void toDomain_createWithPlainDataObject_shouldWork() {
        Instant now = Instant.now();

        // Este es el formato que tu mapper acepta (cuando no hay key "entity")
        AuditOperationEntity entity = AuditOperationEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.CREATE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .dataObject(Map.of("id", 1L, "name", "Centro")) // Sin key "entity"
                .createdAt(now)
                .build();

        // Esto NO debe lanzar excepción (según implementación actual)
        AuditOperation domain = assertDoesNotThrow(() -> mapper.toDomain(entity));

        assertAll(
                () -> assertEquals(1L, domain.getId()),
                () -> assertEquals("Centro", domain.getDataObject().getEntity().get("name")));
    }

    @Test
    @DisplayName("toDomain - DELETE con dataObject plano debe funcionar")
    void toDomain_deleteWithPlainDataObject_shouldWork() {
        Instant now = Instant.now();

        AuditOperationEntity entity = AuditOperationEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.DELETE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .dataObject(Map.of("id", 1L, "name", "Centro Eliminado"))
                .createdAt(now)
                .build();

        AuditOperation domain = assertDoesNotThrow(() -> mapper.toDomain(entity));

        assertEquals("Centro Eliminado", domain.getDataObject().getEntity().get("name"));
    }

    @Test
    @DisplayName("toDomain - ACTIVATE con changes debe funcionar")
    void toDomain_activateWithChanges_shouldWork() {
        Instant now = Instant.now();

        AuditOperationEntity entity = AuditOperationEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.ACTIVATE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .dataObject(Map.of(
                        "changes", Map.of(
                                "active", Map.of("before", false, "after", true))))
                .createdAt(now)
                .build();

        AuditOperation domain = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(OperationType.ACTIVATE, domain.getOperationType()),
                () -> assertTrue(domain.getDataObject().getChanges().containsKey("active")),
                () -> assertEquals(false, domain.getDataObject().getChanges().get("active").getBefore()),
                () -> assertEquals(true, domain.getDataObject().getChanges().get("active").getAfter()));
    }

    @Test
    @DisplayName("toDomain - INACTIVATE con changes debe funcionar")
    void toDomain_inactivateWithChanges_shouldWork() {
        Instant now = Instant.now();

        AuditOperationEntity entity = AuditOperationEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.INACTIVATE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .dataObject(Map.of(
                        "context", Map.of("reason", "baja temporal"),
                        "changes", Map.of(
                                "status", Map.of("before", "ACTIVE", "after", "INACTIVE"))))
                .createdAt(now)
                .build();

        AuditOperation domain = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(OperationType.INACTIVATE, domain.getOperationType()),
                () -> assertTrue(domain.getDataObject().getContext().containsKey("reason")),
                () -> assertEquals("baja temporal", domain.getDataObject().getContext().get("reason")));
    }

}