package com.audit.infrastructure.adapters.output.jpa.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.port.output.AuditSessionRepositoryPort;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditSessionJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditSessionRepository;

import jakarta.persistence.criteria.Predicate;
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
    public List<AuditSession> findByFilters(AuditSessionFilter filter) {
        log.debug("Finding audit sessions with filters: {}", filter);

        Specification<AuditSessionEntity> spec = buildSpecification(filter);

        if (filter.getPage() != null && filter.getSize() != null) {
            PageRequest pageRequest = PageRequest.of(
                    filter.getPage(),
                    filter.getSize(),
                    buildSort(filter));
            return auditSessionRepository.findAll(spec, pageRequest)
                    .getContent()
                    .stream()
                    .map(mapper::toDomain)
                    .toList();
        }

        return auditSessionRepository.findAll(spec, buildSort(filter))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AuditSession> findById(Long id) {
        log.debug("Finding audit session by id: {}", id);
        return auditSessionRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public long countByFilters(AuditSessionFilter filter) {
        log.debug("Counting audit sessions with filters: {}", filter);
        Specification<AuditSessionEntity> spec = buildSpecification(filter);
        return auditSessionRepository.count(spec);
    }

    @Override
    public boolean existsActiveSession(String userId) {
        log.debug("Checking active session for user: {}", userId);
        return auditSessionRepository.existsActiveSession(userId);
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

            // userId filter removed intentionally (not used)

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

            // ipAddress filter removed intentionally (not used)

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(AuditSessionFilter filter) {
        String sortField = filter.getSortField() != null ? filter.getSortField() : "actionAt";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

        return sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
    }
}
