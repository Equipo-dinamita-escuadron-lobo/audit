package com.audit.infrastructure.adapters.output.jpa.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.query.AuditSessionCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;
import com.audit.application.port.output.AuditSessionQueryPort;
import com.audit.domain.enums.UserRole;
import com.audit.domain.model.AuditSession;
import com.audit.domain.port.output.AuditSessionRepositoryPort;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditSessionJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.projection.SessionProjection;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditSessionRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditSessionRepositoryAdapter implements AuditSessionQueryPort,
        AuditSessionRepositoryPort {

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
    public PageResult<CombinedSession> findCombinedSessions(AuditSessionCriteria filter, QueryOptions options) {
        String sanitizedUserName = sanitizeUserName(filter.getUserName());
        Sort sort = buildSortForNativeQuery(options);
        Pageable pageable = PageRequest.of(
                options.getPage(),
                options.getSize(),
                sort);
        Page<SessionProjection> page = auditSessionRepository.findCombinedSessions(
                filter.getDateFrom(),
                filter.getDateTo(),
                sanitizedUserName,
                filter.getUserRole() != null ? filter.getUserRole().name() : null,
                pageable);
        List<CombinedSession> sessions = page.getContent().stream()
                .map(this::projectionToDomain)
                .toList();
        return new PageResult<>(sessions, page.getTotalElements());
    }

    private Sort buildSortForNativeQuery(QueryOptions options) {
        String sortField = options.getSortField() != null ? options.getSortField() : "loginTime";
        String sortDirection = options.getSortDirection() != null ? options.getSortDirection() : "DESC";

        Set<String> allowedFields = Set.of("loginTime", "logoutTime", "userName", "userRole");
        if (!allowedFields.contains(sortField)) {
            sortField = "loginTime";
        }

        if (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)) {
            sortDirection = "DESC";
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return JpaSort.unsafe(direction, sortField);
    }

    private CombinedSession projectionToDomain(SessionProjection projection) {
        return CombinedSession.of(
                projection.getSessionId(),
                projection.getUserName(),
                UserRole.valueOf(projection.getUserRole()),
                projection.getLoginTime(),
                projection.getLogoutTime());
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

    @Override
    public long countByCriteria(AuditSessionCriteria filter) {
        return auditSessionRepository.countCombinedSessions(
                filter.getDateFrom(),
                filter.getDateTo(),
                sanitizeUserName(filter.getUserName()),
                filter.getUserRole() != null ? filter.getUserRole().name() : null);
    }

    @Override
    public List<CombinedSession> findAllForExport(AuditSessionCriteria criteria) {
        return auditSessionRepository.findAllForExport(
                criteria.getDateFrom(),
                criteria.getDateTo(),
                sanitizeUserName(criteria.getUserName()),
                criteria.getUserRole() != null ? criteria.getUserRole().name() : null)
                .stream()
                .map(this::projectionToDomain)
                .toList();
    }
}
