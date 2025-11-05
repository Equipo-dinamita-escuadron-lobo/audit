package com.audit.infrastructure.adapters.output.jpa.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.audit.domain.enums.UserRole;
import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.model.CombinedSession;
import com.audit.domain.model.PageResult;
import com.audit.domain.port.output.AuditSessionRepositoryPort;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditSessionJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.projection.SessionProjection;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditSessionRepository;

import jakarta.persistence.criteria.Predicate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
        log.debug("Saving audit session for user: {}", auditSession.getUserName());
        AuditSessionEntity entity = mapper.toEntity(auditSession);
        AuditSessionEntity saved = auditSessionRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public PageResult<AuditSession> findPageByFilters(AuditSessionFilter filter) {
        Specification<AuditSessionEntity> spec = buildSpecification(filter);
        Sort sort = buildSortForNativeQuery(filter);
        if (filter.getPage() != null && filter.getSize() != null) {
            PageRequest pageRequest = PageRequest.of(
                    filter.getPage(),
                    filter.getSize(),
                    sort);
            Page<AuditSessionEntity> page = auditSessionRepository.findAll(spec, pageRequest);
            List<AuditSession> content = page.getContent()
                    .stream()
                    .map(mapper::toDomain)
                    .toList();
            return new PageResult<>(content, page.getTotalElements());
        }

        List<AuditSession> all = auditSessionRepository.findAll(spec, sort)
                .stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(all, all.size());
    }

    @Override
    public Optional<AuditSession> findById(Long id) {
        log.debug("Finding audit session by id: {}", id);
        return auditSessionRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsActiveSession(String userId) {
        log.debug("Checking active session for user: {}", userId);
        return auditSessionRepository.existsActiveSession(userId);
    }

    private Sort buildSortForNativeQuery(AuditSessionFilter filter) {
        String sortField = filter.getSortField() != null ? filter.getSortField() : "actionAt";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";
        
        // Mapear campos de la entidad a aliases de la consulta SQL nativa
        String mappedSortField = switch (sortField) {
            case "actionAt" -> "loginTime";  // actionAt de la entidad → loginTime en SQL
            case "userName" -> "userName";
            case "userRole" -> "userRole";
            default -> "loginTime";
        };
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        // JpaSort.unsafe permite usar aliases de consultas SQL nativas
        return JpaSort.unsafe(direction, mappedSortField);
    }

    @Override
    public Optional<AuditSession> findBySessionId(String sessionId) {
        log.debug("Finding audit session by sessionId: {}", sessionId);
        return auditSessionRepository.findBySessionId(sessionId)
                .map(mapper::toDomain);
    }

    private CombinedSession projectionToDomain(SessionProjection projection) {
        ZonedDateTime loginTime = projection.getLoginTime() != null 
            ? ZonedDateTime.ofInstant(projection.getLoginTime(), ZoneId.of("America/Bogota"))
            : null;
        
        ZonedDateTime logoutTime = projection.getLogoutTime() != null 
            ? ZonedDateTime.ofInstant(projection.getLogoutTime(), ZoneId.of("America/Bogota"))
            : null;
        return CombinedSession.reconstruct(
                projection.getSessionId(),
                projection.getUserName(),
                projection.getUserRole(),
                loginTime,
                logoutTime
        );
    }

    @Override
    public PageResult<CombinedSession> findCombinedSessions(AuditSessionFilter filter) {
        Sort sort = buildSortForNativeQuery(filter);
        Pageable pageable = PageRequest.of(
            filter.getPage(),
            filter.getSize(),
            sort
        );
        
        Page<SessionProjection> page = auditSessionRepository.findCombinedSessions(
            filter.getDateFrom(),
            filter.getDateTo(),
            filter.getUserName(),
            filter.getUserRole() != null ? filter.getUserRole().name() : null,
            filter.getRequestingUserRole(),
            pageable
        );

        List<CombinedSession> sessions = page.getContent().stream()
            .map(this::projectionToDomain)
            .toList();
            
        return new PageResult<>(sessions, page.getTotalElements());
    
    }

    private Specification<AuditSessionEntity> buildSpecification(AuditSessionFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("actionAt"), filter.getDateFrom()));
            }

            if (filter.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("actionAt"), filter.getDateTo()));
            }

            if (filter.getUserName() != null && !filter.getUserName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("userName")),
                        "%" + filter.getUserName().toLowerCase() + "%"));
            }

            if (filter.getUserRole() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("userRole"), filter.getUserRole()));
            }

            if (filter.getAction() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("action"), filter.getAction()));
            }

            if ("PROFESOR".equals(filter.getRequestingUserRole())) {
                predicates.add(criteriaBuilder.notEqual(
                        root.get("userRole"), UserRole.ADMIN));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
