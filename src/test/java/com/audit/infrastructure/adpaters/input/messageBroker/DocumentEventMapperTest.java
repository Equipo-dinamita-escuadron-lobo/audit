package com.audit.infrastructure.adpaters.input.messageBroker;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.dto.request.LogDocumentEventRequest;
import com.audit.domain.enums.DocumentOperationType;
import com.audit.infrastructure.adapters.input.messageBroker.dto.DocumentEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.DocumentEventMapper;
import com.audit.infrastructure.adapters.output.exception.security.AuditMappingException;

class DocumentEventMapperTest {

    private final DocumentEventMapper mapper = new DocumentEventMapper();

    private DocumentEventDto validDto() {
        return DocumentEventDto.builder()
                .enterpriseId("ENT-001")
                .documentId("DOC-001")
                .documentCode("FAC-001")
                .documentType("FACTURA")
                .documentDate(LocalDate.now())
                .userId("USER-001")
                .userName("Juan")
                .userRoles(List.of("ADMIN"))
                .operationType("CREATE")
                .moduleName("VENTAS")
                .operationAt(Instant.now())
                .documentData(Map.of("header", Map.of("total", 1000)))
                .build();
    }

    @Test
    @DisplayName("toRequest - debe mapear todos los campos correctamente")
    void toRequest_validDto_shouldMapAllFields() {
        LogDocumentEventRequest result = mapper.toRequest(validDto());

        assertAll(
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals("DOC-001", result.getDocumentId()),
                () -> assertEquals("FAC-001", result.getDocumentCode()),
                () -> assertEquals("FACTURA", result.getDocumentType()),
                () -> assertEquals("USER-001", result.getUserId()),
                () -> assertEquals("Juan", result.getUserName()),
                () -> assertEquals(List.of("ADMIN"), result.getUserRoles()),
                () -> assertEquals(DocumentOperationType.CREATE, result.getOperationType()),
                () -> assertEquals("VENTAS", result.getModuleName()),
                () -> assertNotNull(result.getDocumentData()));
    }

    @Test
    @DisplayName("toRequest - operationType en minúsculas debe parsearse correctamente")
    void toRequest_lowercaseOperationType_shouldParse() {
        DocumentEventDto dto = validDto();
        dto.setOperationType("create");

        LogDocumentEventRequest result = mapper.toRequest(dto);

        assertEquals(DocumentOperationType.CREATE, result.getOperationType());
    }

    @Test
    @DisplayName("toRequest - operationType inválido debe lanzar AuditMappingException")
    void toRequest_invalidOperationType_shouldThrow() {
        DocumentEventDto dto = validDto();
        dto.setOperationType("INVALID_OP");

        assertThrows(AuditMappingException.class, () -> mapper.toRequest(dto));
    }

    @Test
    @DisplayName("toRequest - operationType null debe lanzar AuditMappingException")
    void toRequest_nullOperationType_shouldThrow() {
        DocumentEventDto dto = validDto();
        dto.setOperationType(null);

        assertThrows(AuditMappingException.class, () -> mapper.toRequest(dto));
    }

    @Test
    @DisplayName("toRequest - documentData nulo debe lanzar AuditMappingException")
    void toRequest_nullDocumentData_shouldThrow() {
        DocumentEventDto dto = validDto();
        dto.setDocumentData(null);

        assertThrows(AuditMappingException.class, () -> mapper.toRequest(dto));
    }

    @Test
    @DisplayName("toRequest - documentData vacío debe lanzar AuditMappingException")
    void toRequest_emptyDocumentData_shouldThrow() {
        DocumentEventDto dto = validDto();
        dto.setDocumentData(Collections.emptyMap());

        assertThrows(AuditMappingException.class, () -> mapper.toRequest(dto));
    }
}
