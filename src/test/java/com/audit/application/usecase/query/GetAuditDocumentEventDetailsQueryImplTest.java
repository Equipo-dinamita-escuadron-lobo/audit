package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.GetDocumentEventDetailRequest;
import com.audit.application.dto.response.DocumentEventDetailResponse;
import com.audit.application.port.output.AuditDocumentEventQueryPort;
import com.audit.application.usecases.queries.GetAuditDocumentEventDetailsQueryImpl;
import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.model.AuditDocumentEvent;
import com.audit.domain.model.DocumentData;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuditDocumentEventDetailsQueryImplTest {

    @Mock
    private AuditDocumentEventQueryPort queryPort;

    @InjectMocks
    private GetAuditDocumentEventDetailsQueryImpl useCase;

    @Test
    @DisplayName("execute - debe retornar detalle de eventos por código de documento")
    void execute_validRequest_shouldReturnDetails() {
        DocumentData data = DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                List.of(Map.of("account", "1105")),
                Map.of("total", 1000),
                Map.of("source", "sales"));

        AuditDocumentEvent event = AuditDocumentEvent.reconstruct(
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
                data,
                Instant.now());

        when(queryPort.findEventsByDocumentCode("ENT-001", "FAC-001"))
                .thenReturn(List.of(event));

        GetDocumentEventDetailRequest request = GetDocumentEventDetailRequest.builder()
                .enterpriseId("ENT-001")
                .documentCode("FAC-001")
                .build();

        List<DocumentEventDetailResponse> response = useCase.execute(request);

        assertAll(
                () -> assertEquals(1, response.size()),
                () -> assertEquals("CREATE", response.get(0).getOperationType()),
                () -> assertEquals("Freider", response.get(0).getUserName()),
                () -> assertEquals(List.of("ADMIN"), response.get(0).getUserRoles()),
                () -> assertEquals(data, response.get(0).getDocumentData()));

        verify(queryPort).findEventsByDocumentCode("ENT-001", "FAC-001");
    }
}
