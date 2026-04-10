package com.audit.infrastructure.adapters.output.jpa.adapter;

import com.audit.application.internal.PageResult;
import com.audit.application.internal.QueryOptions;
import com.audit.domain.enums.UserRole;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.AuditOperationCriteria;
import com.audit.domain.port.output.AuditOperationRepositoryPort;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditOperationEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditOperationJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditOperationRepositoryAdapter implements AuditOperationRepositoryPort {

    private final IAuditOperationRepository auditOperationRepository;
    private final AuditOperationJpaMapper mapper;

    @Override
    public AuditOperation save(AuditOperation auditOperation) {
        AuditOperationEntity entity = mapper.toEntity(auditOperation);
        AuditOperationEntity saved = auditOperationRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public PageResult<AuditOperation> findPageByCriteria(AuditOperationCriteria criteria, QueryOptions options) {
        Specification<AuditOperationEntity> spec = buildSpecification(criteria).and(buildRoleFilter(options));
        Sort sort = buildSort(options);
        PageRequest pageRequest = PageRequest.of(options.getPage(), options.getSize(), sort);
        Page<AuditOperationEntity> page = auditOperationRepository.findAll(spec, pageRequest);
        List<AuditOperation> content = page.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new PageResult<>(content, page.getTotalElements());
    }

    @Override
    public Optional<AuditOperation> findById(Long id) {
        return auditOperationRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<AuditOperation> findByRegisterId(String registerId, String affectedTable) {
        return auditOperationRepository.findByRegisterIdAndAffectedTable(registerId, affectedTable)
                .map(mapper::toDomain);
    }

    /**
     * Se construye la especificacion con todos los filtros
     */
    private Specification<AuditOperationEntity> buildSpecification(AuditOperationCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getEnterpriseId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("enterpriseId"), criteria.getEnterpriseId()));
            }

            if (criteria.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("operationAt"), criteria.getDateFrom()));
            }

            if (criteria.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("operationAt"), criteria.getDateTo()));
            }

            if (criteria.hasModuleNameCriteria()) {
                String searchTerm = criteria.getModuleName().toLowerCase().trim();
                if (searchTerm.length() < 3) {
                    throw new IllegalArgumentException("moduleName filter must be at least 3 characters");
                }
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("moduleName")),
                        searchTerm + "%"));
            }

            if (criteria.hasAffectedTableCriteria()) {
                String searchTerm = criteria.getAffectedTable().toLowerCase().trim();
                if (searchTerm.length() < 3) {
                    throw new IllegalArgumentException("affectedTable filter must be at least 3 characters");
                }
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("affectedTable")),
                        searchTerm + "%"));
            }

            if (criteria.hasUserNameCriteria()) {
                String searchTerm = criteria.getUserName().toLowerCase().trim();
                if (searchTerm.length() < 3) {
                    throw new IllegalArgumentException("userName filter must be at least 3 characters");
                }
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("userName")),
                        searchTerm + "%"));
            }

            if (criteria.hasUserRoleCriteria()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("userRole"), criteria.getUserRole()));
            }

            if (criteria.hasOperationTypeCriteria()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("operationType"), criteria.getOperationType()));
            }

            if (criteria.hasRegisterIdCriteria()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("registerId"), criteria.getRegisterId()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<AuditOperationEntity> buildRoleFilter(QueryOptions options) {
    return (root, query, cb) -> {
        if (options.getRequestingUserRole() == UserRole.PROFESOR ||
            options.getRequestingUserRole() == UserRole.ESTUDIANTE) {
            return cb.notEqual(root.get("userRole"), UserRole.ADMINISTRADOR);
        }
        return cb.conjunction(); 
    };
}

    private Sort buildSort(QueryOptions options) {
        String sortField = options.getSortField() != null ? options.getSortField() : "operationAt";
        String sortDirection = options.getSortDirection() != null ? options.getSortDirection() : "DESC";

        Set<String> allowedFields = Set.of(
                "operationAt", "userName", "userRole",
                "operationType", "moduleName", "affectedTable");

        if (!allowedFields.contains(sortField)) {
            sortField = "operationAt";
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortField);
    }

    @Override
    public long countByCriteria(AuditOperationCriteria filter) {
        Specification<AuditOperationEntity> spec = buildSpecification(filter);
        return auditOperationRepository.count(spec);
    }

    // @Override
    // public List<AuditOperation> findForEsxport(AuditOperationCriteria filter) {
    //     Specification<AuditOperationEntity> spec = buildSpecification(filter);
    //     Sort sort = buildSort(filter);
    //     return auditOperationRepository.findAll(spec, sort)
    //             .stream()
    //             .map(mapper::toDomain);
    // }

    @Override
    public List<AuditOperation> findForExport(AuditOperationCriteria criteria) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findForExport'");
    }
}
