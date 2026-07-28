package com.audit.infrastructure.adpaters.input.messageBroker;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.dto.request.LogOperationRequest;
import com.audit.domain.enums.OperationType;
import com.audit.infrastructure.adapters.input.messageBroker.dto.OperationEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.OperationEventMapper;
import com.audit.infrastructure.adapters.output.exception.security.AuditMappingException;

class OperationEventMapperTest {

    private final OperationEventMapper mapper = new OperationEventMapper();

    private OperationEventDto createDto() {
        return OperationEventDto.builder()
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType("CREATE")
                .operationAt(Instant.now())
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("REG-001")
                .dataObject(Map.of("entity", Map.of("id", 1, "name", "Centro")))
                .build();
    }

    private OperationEventDto updateDto() {
        return OperationEventDto.builder()
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType("UPDATE")
                .operationAt(Instant.now())
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("REG-001")
                .dataObject(Map.of("changes", Map.of(
                        "name", Map.of("before", "Viejo", "after", "Nuevo"))))
                .build();
    }

    @Test
    @DisplayName("toRequest - CREATE debe mapear entity correctamente")
    void toRequest_createOperation_shouldMapEntity() {
        LogOperationRequest result = mapper.toRequest(createDto());

        assertAll(
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals(OperationType.CREATE, result.getOperationType()),
                () -> assertNotNull(result.getDataObject()),
                () -> assertFalse(result.getDataObject().getEntity().isEmpty()));
    }

    @Test
    @DisplayName("toRequest - UPDATE debe mapear changes correctamente")
    void toRequest_updateOperation_shouldMapChanges() {
        LogOperationRequest result = mapper.toRequest(updateDto());

        assertAll(
                () -> assertEquals(OperationType.UPDATE, result.getOperationType()),
                () -> assertFalse(result.getDataObject().getChanges().isEmpty()),
                () -> assertTrue(result.getDataObject().getEntity().isEmpty()));
    }

    @Test
    @DisplayName("toRequest - UPDATE con context debe incluir context")
    void toRequest_updateWithContext_shouldMapContext() {
        OperationEventDto dto = updateDto();
        dto.setDataObject(Map.of(
                "context", Map.of("invoiceId", "FAC-001"),
                "changes", Map.of("status", Map.of("before", "ACTIVE", "after", "INACTIVE"))));

        LogOperationRequest result = mapper.toRequest(dto);

        assertFalse(result.getDataObject().getContext().isEmpty());
    }

    @Test
    @DisplayName("toRequest - DELETE debe mapear entity igual que CREATE")
    void toRequest_deleteOperation_shouldMapEntity() {
        OperationEventDto dto = createDto();
        dto.setOperationType("DELETE");

        LogOperationRequest result = mapper.toRequest(dto);

        assertEquals(OperationType.DELETE, result.getOperationType());
        assertFalse(result.getDataObject().getEntity().isEmpty());
    }

    @Test
    @DisplayName("toRequest - UPDATE sin changes debe lanzar AuditMappingException")
    void toRequest_updateWithoutChanges_shouldThrow() {
        OperationEventDto dto = updateDto();
        dto.setDataObject(Map.of("context", Map.of("key", "val")));

        assertThrows(AuditMappingException.class, () -> mapper.toRequest(dto));
    }

    @Test
    @DisplayName("toRequest - operationType inválido debe lanzar AuditMappingException")
    void toRequest_invalidOperationType_shouldThrow() {
        OperationEventDto dto = createDto();
        dto.setOperationType("UNKNOWN");

        assertThrows(AuditMappingException.class, () -> mapper.toRequest(dto));
    }

    @Test
    @DisplayName("sanitizeText - debe eliminar caracteres peligrosos del userName")
    void toRequest_dirtyUserName_shouldBeSanitized() {
        OperationEventDto dto = createDto();
        dto.setUserName("  Juan\n<script>  ");

        LogOperationRequest result = mapper.toRequest(dto);

        assertAll(
                () -> assertFalse(result.getUserName().contains("\n")),
                () -> assertFalse(result.getUserName().contains("<")),
                () -> assertFalse(result.getUserName().contains(">")),
                () -> assertEquals("Juan script", result.getUserName().trim()
                        .replaceAll("\\s+", " ")));
    }
}
