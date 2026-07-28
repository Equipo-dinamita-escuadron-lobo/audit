package com.audit.application.usecase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.LogSessionRequest;
import com.audit.application.usecases.commands.LogAuditSessionCommandImpl;
import com.audit.domain.enums.UserAction;
import com.audit.domain.model.AuditSession;
import com.audit.domain.port.output.AuditSessionRepositoryPort;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogAuditSessionCommandImplTest {

    @Mock
    private AuditSessionRepositoryPort auditSessionRepository;

    @InjectMocks
    private LogAuditSessionCommandImpl useCase;

    @Test
    @DisplayName("execute - debe crear y guardar evento de sesión")
    void execute_validRequest_shouldSaveSession() {
        Instant now = Instant.now();

        LogSessionRequest request = LogSessionRequest.builder()
                .sessionId("SESSION-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .action(UserAction.LOGIN)
                .actionAt(now)
                .ipAddress("127.0.0.1")
                .build();

        useCase.execute(request);

        ArgumentCaptor<AuditSession> captor = ArgumentCaptor.forClass(AuditSession.class);
        verify(auditSessionRepository).save(captor.capture());

        AuditSession saved = captor.getValue();

        assertAll(
                () -> assertEquals("SESSION-001", saved.getSessionId()),
                () -> assertEquals("USER-001", saved.getUserId()),
                () -> assertEquals("Freider", saved.getUserName()),
                () -> assertEquals(List.of("ADMIN"), saved.getUserRole()),
                () -> assertEquals(UserAction.LOGIN, saved.getAction()),
                () -> assertEquals(now, saved.getActionAt()),
                () -> assertEquals("127.0.0.1", saved.getIpAddress()));
    }
}
