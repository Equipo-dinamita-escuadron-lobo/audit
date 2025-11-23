package com.audit.infrastructure.adapters.output.jpa.mapper;

import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.audit.domain.model.AuditSession;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;

@Component
public class AuditSessionJpaMapper {

    public AuditSession toDomain(AuditSessionEntity entity) {
        return AuditSession.reconstruct(
                entity.getId(),
                entity.getSessionId(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserRole(),
                entity.getAction(),
                entity.getActionAt().atZone(ZoneId.of("UTC")),
                entity.getIpAddress(),
                entity.getCreatedAt().atZone(ZoneId.of("UTC")));
    }

    public AuditSessionEntity toEntity(AuditSession domain) {
        return AuditSessionEntity.builder()
                .id(domain.getId())
                .sessionId(domain.getSessionId())
                .userId(domain.getUserId())
                .userName(domain.getUserName())
                .userRole(domain.getUserRole())
                .action(domain.getAction())
                .actionAt(domain.getActionAt().toInstant())
                .ipAddress(domain.getIpAddress())
                .createdAt(domain.getCreatedAt().toInstant())
                .build();
    }
}
