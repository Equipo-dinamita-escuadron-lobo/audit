package com.audit.application.usecase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.LogDocumentEventRequest;
import com.audit.application.usecases.commands.LogAuditDocumentEventCommandImpl;
import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.model.AuditDocumentEvent;
import com.audit.domain.model.DocumentData;
import com.audit.domain.port.output.AuditDocumentEventRepositoryPort;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogAuditDocumentEventCommandImplTest {

    @Mock
    private AuditDocumentEventRepositoryPort auditDocumentEventRepository;

    @InjectMocks
    private LogAuditDocumentEventCommandImpl useCase;

    @Test
    @DisplayName("execute - debe crear y guardar evento documental")
    void execute_validRequest_shouldSaveDocumentEvent() {
        Instant now = Instant.now();
        LocalDate documentDate = LocalDate.now();

        DocumentData data = DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                List.of(Map.of("account", "1105", "debit", 1000)),
                Map.of("total", 1000),
                Map.of("source", "sales"));

        LogDocumentEventRequest request = LogDocumentEventRequest.builder()
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
                .documentData(data)
                .build();

        useCase.execute(request);

        ArgumentCaptor<AuditDocumentEvent> captor = ArgumentCaptor.forClass(AuditDocumentEvent.class);
        verify(auditDocumentEventRepository).save(captor.capture());

        AuditDocumentEvent saved = captor.getValue();

        assertAll(
                () -> assertEquals("ENT-001", saved.getEnterpriseId()),
                () -> assertEquals("FAC-001", saved.getDocumentCode()),
                () -> assertEquals("FACTURA", saved.getDocumentType()),
                () -> assertEquals("DOC-001", saved.getDocumentId()),
                () -> assertEquals("USER-001", saved.getUserId()),
                () -> assertEquals("Freider", saved.getUserName()),
                () -> assertEquals(List.of("ADMIN"), saved.getUserRoles()),
                () -> assertEquals(DocumentOperationType.CREATE, saved.getOperationType()),
                () -> assertEquals("TP-001", saved.getThirdPartyId()),
                () -> assertEquals("Cliente prueba", saved.getThirdPartyName()),
                () -> assertEquals("DOCUMENTS", saved.getModuleName()),
                () -> assertEquals(now, saved.getOperationAt()),
                () -> assertEquals(documentDate, saved.getDocumentDate()),
                () -> assertEquals(data, saved.getDocumentData()));
    }
}
