package com.audit.infrastructure.adpaters.output.jpa.adapter;

import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.query.AuditSessionCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;
import com.audit.domain.enums.UserAction;
import com.audit.domain.model.AuditSession;
import com.audit.infrastructure.adapters.output.jpa.adapter.AuditSessionRepositoryAdapter;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditSessionJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.projection.SessionProjection;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditSessionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.data.domain.*;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditSessionRepositoryAdapterTest {

    @Mock
    private IAuditSessionRepository auditSessionRepository;

    private final AuditSessionJpaMapper mapper = new AuditSessionJpaMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("save - debe convertir dominio a entidad, guardar y retornar dominio")
    void save_validSession_shouldPersistAndReturnDomain() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        Instant now = Instant.now();

        AuditSession domain = AuditSession.reconstruct(
                null,
                "SESSION-001",
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                UserAction.LOGIN,
                now,
                "127.0.0.1",
                now);

        AuditSessionEntity savedEntity = AuditSessionEntity.builder()
                .id(1L)
                .sessionId("SESSION-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .action(UserAction.LOGIN)
                .actionAt(now)
                .ipAddress("127.0.0.1")
                .createdAt(now)
                .build();

        when(auditSessionRepository.save(any(AuditSessionEntity.class))).thenReturn(savedEntity);

        AuditSession result = adapter.save(domain);

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("SESSION-001", result.getSessionId()),
                () -> assertEquals("USER-001", result.getUserId()),
                () -> assertEquals(UserAction.LOGIN, result.getAction()));

        verify(auditSessionRepository).save(any(AuditSessionEntity.class));
    }

    @Test
    @DisplayName("findBySessionId - si existe debe retornar dominio")
    void findBySessionId_existingSession_shouldReturnDomain() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        Instant now = Instant.now();

        AuditSessionEntity entity = AuditSessionEntity.builder()
                .id(1L)
                .sessionId("SESSION-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .action(UserAction.LOGIN)
                .actionAt(now)
                .ipAddress("127.0.0.1")
                .createdAt(now)
                .build();

        when(auditSessionRepository.findBySessionId("SESSION-001"))
                .thenReturn(Optional.of(entity));

        Optional<AuditSession> result = adapter.findBySessionId("SESSION-001");

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals("SESSION-001", result.get().getSessionId()),
                () -> assertEquals("Freider", result.get().getUserName()));
    }

    @Test
    @DisplayName("findBySessionId - si no existe debe retornar vacío")
    void findBySessionId_notFound_shouldReturnEmpty() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        when(auditSessionRepository.findBySessionId("NO-EXISTE"))
                .thenReturn(Optional.empty());

        Optional<AuditSession> result = adapter.findBySessionId("NO-EXISTE");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findCombinedSessions - debe sanitizar userName, paginar, parsear roles y retornar PageResult")
    void findCombinedSessions_validCriteria_shouldReturnPageResult() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();
        Instant login = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant logout = Instant.now();

        SessionProjection projection = mock(SessionProjection.class);
        when(projection.getSessionId()).thenReturn("SESSION-001");
        when(projection.getUserName()).thenReturn("Freider");
        when(projection.getUserRole()).thenReturn("[\"ADMIN\",\"TEACHER\"]");
        when(projection.getLoginTime()).thenReturn(login);
        when(projection.getLogoutTime()).thenReturn(logout);

        Page<SessionProjection> page = new PageImpl<>(
                List.of(projection),
                PageRequest.of(0, 20),
                1);

        when(auditSessionRepository.findCombinedSessions(
                eq(from),
                eq(to),
                eq("Freider"),
                eq("ADMIN"),
                any(Pageable.class))).thenReturn(page);

        AuditSessionCriteria criteria = AuditSessionCriteria.create(
                from,
                to,
                " Freider ",
                "ADMIN",
                null);

        QueryOptions options = QueryOptions.builder()
                .page(0)
                .size(20)
                .sortField("loginTime")
                .sortDirection("DESC")
                .build();

        PageResult<CombinedSession> result = adapter.findCombinedSessions(criteria, options);

        assertAll(
                () -> assertEquals(1, result.getContent().size()),
                () -> assertEquals(1L, result.getTotalElements()),
                () -> assertEquals("SESSION-001", result.getContent().get(0).getSessionId()),
                () -> assertEquals("Freider", result.getContent().get(0).getUserName()),
                () -> assertEquals(List.of("ADMIN", "TEACHER"), result.getContent().get(0).getUserRole()),
                () -> assertEquals(login, result.getContent().get(0).getLoginTime()),
                () -> assertEquals(logout, result.getContent().get(0).getLogoutTime()));
    }

    @Test
    @DisplayName("findCombinedSessions - userName menor a 3 caracteres debe fallar")
    void findCombinedSessions_shortUserName_shouldThrowException() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        AuditSessionCriteria criteria = AuditSessionCriteria.create(
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now(),
                "Fr",
                null,
                null);

        QueryOptions options = QueryOptions.builder()
                .page(0)
                .size(20)
                .sortField("loginTime")
                .sortDirection("DESC")
                .build();

        assertThrows(IllegalArgumentException.class, () -> adapter.findCombinedSessions(criteria, options));

        verifyNoInteractions(auditSessionRepository);
    }

    @Test
    @DisplayName("findCombinedSessions - roles JSON inválidos deben retornar lista vacía")
    void findCombinedSessions_invalidRolesJson_shouldReturnEmptyRoles() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();

        SessionProjection projection = mock(SessionProjection.class);
        when(projection.getSessionId()).thenReturn("SESSION-001");
        when(projection.getUserName()).thenReturn("Freider");
        when(projection.getUserRole()).thenReturn("INVALID_JSON");
        when(projection.getLoginTime()).thenReturn(Instant.now());
        when(projection.getLogoutTime()).thenReturn(null);

        when(auditSessionRepository.findCombinedSessions(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(projection)));

        AuditSessionCriteria criteria = AuditSessionCriteria.create(from, to, null, null, null);

        QueryOptions options = QueryOptions.builder()
                .page(0)
                .size(20)
                .sortField("unknownField")
                .sortDirection("BAD")
                .build();

        PageResult<CombinedSession> result = adapter.findCombinedSessions(criteria, options);

        assertAll(
                () -> assertEquals(1, result.getContent().size()),
                () -> assertTrue(result.getContent().get(0).getUserRole().isEmpty()),
                () -> assertTrue(result.getContent().get(0).isActive()));
    }

    @Test
    @DisplayName("countByCriteria - debe delegar conteo al repositorio con filtros sanitizados")
    void countByCriteria_validCriteria_shouldDelegateToRepository() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        when(auditSessionRepository.countCombinedSessions(from, to, "Freider", "ADMIN"))
                .thenReturn(5L);

        AuditSessionCriteria criteria = AuditSessionCriteria.create(
                from,
                to,
                " Freider ",
                "ADMIN",
                null);

        long result = adapter.countByCriteria(criteria);

        assertEquals(5L, result);
    }

    @Test
    @DisplayName("findAllForExport - debe consultar proyecciones y mapearlas a CombinedSession")
    void findAllForExport_validCriteria_shouldReturnSessions() {
        AuditSessionRepositoryAdapter adapter = new AuditSessionRepositoryAdapter(auditSessionRepository, mapper,
                objectMapper);

        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        SessionProjection projection = mock(SessionProjection.class);
        when(projection.getSessionId()).thenReturn("SESSION-001");
        when(projection.getUserName()).thenReturn("Freider");
        when(projection.getUserRole()).thenReturn("[\"ADMIN\"]");
        when(projection.getLoginTime()).thenReturn(Instant.now());
        when(projection.getLogoutTime()).thenReturn(null);

        when(auditSessionRepository.findAllForExport(from, to, null, null))
                .thenReturn(List.of(projection));

        AuditSessionCriteria criteria = AuditSessionCriteria.create(from, to, null, null, null);

        List<CombinedSession> result = adapter.findAllForExport(criteria);

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("SESSION-001", result.get(0).getSessionId()),
                () -> assertEquals(List.of("ADMIN"), result.get(0).getUserRole()));
    }
}
