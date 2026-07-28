package com.audit.infrastructure.adpaters.output.jpa.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.UserAction;
import com.audit.domain.model.AuditSession;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditSessionJpaMapper;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditSessionJpaMapperTest {

    private final AuditSessionJpaMapper mapper = new AuditSessionJpaMapper();

    @Test
    @DisplayName("toEntity - debe mapear dominio a entidad")
    void toEntity_validDomain_shouldMapAllFields() {
        Instant now = Instant.now();

        AuditSession domain = AuditSession.reconstruct(
                1L,
                "SESSION-001",
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                UserAction.LOGIN,
                now,
                "127.0.0.1",
                now);

        AuditSessionEntity entity = mapper.toEntity(domain);

        assertAll(
                () -> assertEquals(1L, entity.getId()),
                () -> assertEquals("SESSION-001", entity.getSessionId()),
                () -> assertEquals("USER-001", entity.getUserId()),
                () -> assertEquals("Freider", entity.getUserName()),
                () -> assertEquals(List.of("ADMIN"), entity.getUserRole()),
                () -> assertEquals(UserAction.LOGIN, entity.getAction()),
                () -> assertEquals(now, entity.getActionAt()),
                () -> assertEquals("127.0.0.1", entity.getIpAddress()),
                () -> assertEquals(now, entity.getCreatedAt()));
    }

    @Test
    @DisplayName("toDomain - debe mapear entidad a dominio")
    void toDomain_validEntity_shouldMapAllFields() {
        Instant now = Instant.now();

        AuditSessionEntity entity = AuditSessionEntity.builder()
                .id(1L)
                .sessionId("SESSION-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .action(UserAction.LOGOUT)
                .actionAt(now)
                .ipAddress("127.0.0.1")
                .createdAt(now)
                .build();

        AuditSession domain = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(1L, domain.getId()),
                () -> assertEquals("SESSION-001", domain.getSessionId()),
                () -> assertEquals("USER-001", domain.getUserId()),
                () -> assertEquals("Freider", domain.getUserName()),
                () -> assertEquals(List.of("ADMIN"), domain.getUserRole()),
                () -> assertEquals(UserAction.LOGOUT, domain.getAction()),
                () -> assertEquals(now, domain.getActionAt()),
                () -> assertEquals("127.0.0.1", domain.getIpAddress()),
                () -> assertEquals(now, domain.getCreatedAt()));
    }
}
