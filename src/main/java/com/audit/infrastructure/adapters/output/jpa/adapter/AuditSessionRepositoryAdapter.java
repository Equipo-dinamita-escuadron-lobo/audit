package com.audit.infrastructure.adapters.output.jpa.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.model.CombinedSession;
import com.audit.domain.model.PageResult;
import com.audit.domain.port.output.AuditSessionRepositoryPort;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditSessionJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.projection.SessionProjection;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditSessionRepository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditSessionRepositoryAdapter implements AuditSessionRepositoryPort {

    private final IAuditSessionRepository auditSessionRepository;
    private final AuditSessionJpaMapper mapper;

    @Override
    public AuditSession save(AuditSession auditSession) {
        AuditSessionEntity entity = mapper.toEntity(auditSession);
        AuditSessionEntity saved = auditSessionRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AuditSession> findBySessionId(String sessionId) {
        return auditSessionRepository.findBySessionId(sessionId)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<CombinedSession> findCombinedSessions(AuditSessionFilter filter) {
        String sanitizedUserName = sanitizeUserName(filter.getUserName());
        Sort sort = buildSortForNativeQuery(filter);
        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                sort);
        Page<SessionProjection> page = auditSessionRepository.findCombinedSessions(
                filter.getDateFrom(),
                filter.getDateTo(),
                sanitizedUserName,
                filter.getUserRole() != null ? filter.getUserRole().name() : null,
                filter.getRequestingUserRole(),
                pageable);
        List<CombinedSession> sessions = page.getContent().stream()
                .map(this::projectionToDomain)
                .toList();
        return new PageResult<>(sessions, page.getTotalElements());
    }

    private Sort buildSortForNativeQuery(AuditSessionFilter filter) {

        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";
        if (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)) {
            sortDirection = "DESC";
        }
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return JpaSort.unsafe(direction, "loginTime");
    }

    private CombinedSession projectionToDomain(SessionProjection projection) {
        ZonedDateTime loginTime = instantToZonedDateTime(projection.getLoginTime());
        ZonedDateTime logoutTime = instantToZonedDateTime(projection.getLogoutTime());

        return CombinedSession.reconstruct(
                projection.getSessionId(),
                projection.getUserName(),
                projection.getUserRole(),
                loginTime,
                logoutTime);
    }

    private String sanitizeUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return null;
        }
        String sanitized = userName.trim();
        if (sanitized.length() < 3) {
            throw new IllegalArgumentException("userName filter must be at least 3 characters");
        }
        sanitized = sanitized.replace("_", "\\_").replace("%", "\\%");
        return sanitized;
    }

    private ZonedDateTime instantToZonedDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneOffset.UTC);
    }
}
