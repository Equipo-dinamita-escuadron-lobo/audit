package com.audit.application.usecase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.LogOperationRequest;
import com.audit.application.usecases.commands.LogAuditOperationCommandImpl;
import com.audit.domain.enums.OperationType;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.OperationData;
import com.audit.domain.port.output.AuditOperationRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogAuditOperationCommandImplTest {

    @Mock
    private AuditOperationRepositoryPort auditOperationRepository;

    @InjectMocks
    private LogAuditOperationCommandImpl useCase;

    @Test
    @DisplayName("execute - debe crear y guardar evento de operación")
    void execute_validRequest_shouldSaveOperation() {
        Instant now = Instant.now();

        OperationData data = OperationData.forCreate(Map.of(
                "id", 1L,
                "name", "Centro"));

        LogOperationRequest request = LogOperationRequest.builder()
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.CREATE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .enterpriseId("ENT-001")
                .dataObject(data)
                .build();

        useCase.execute(request);

        ArgumentCaptor<AuditOperation> captor = ArgumentCaptor.forClass(AuditOperation.class);
        verify(auditOperationRepository).save(captor.capture());

        AuditOperation saved = captor.getValue();

        assertAll(
                () -> assertEquals("USER-001", saved.getUserId()),
                () -> assertEquals("Freider", saved.getUserName()),
                () -> assertEquals(List.of("ADMIN"), saved.getUserRole()),
                () -> assertEquals(OperationType.CREATE, saved.getOperationType()),
                () -> assertEquals("CONFIGURATION", saved.getModuleName()),
                () -> assertEquals("cost_centers", saved.getAffectedTable()),
                () -> assertEquals("1", saved.getRegisterId()),
                () -> assertEquals("ENT-001", saved.getEnterpriseId()),
                () -> assertEquals(data, saved.getDataObject()));
    }
}
