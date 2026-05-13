package com.audit.infrastructure.adapters.output.jpa.adapter;

import com.audit.application.internal.query.AuditOperationCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;
import com.audit.application.port.output.AuditOperationQueryPort;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.ModuleTable;
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
public class AuditOperationRepositoryAdapter implements AuditOperationQueryPort,
        AuditOperationRepositoryPort {

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
        Specification<AuditOperationEntity> spec = buildSpecification(criteria);
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
    public List<ModuleTable> findDistinctModulesAndTables(String enterpriseId) {
        return auditOperationRepository.findDistinctModulesAndTables(enterpriseId)
                .stream()
                .map(p -> new ModuleTable(
                        p.getModuleName(),
                        p.getAffectedTable()))
                .toList();
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
                predicates.add(criteriaBuilder.equal(
                        root.get("moduleName"), criteria.getModuleName()));
            }

            if (criteria.hasAffectedTableCriteria()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("affectedTable"), criteria.getAffectedTable()));
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
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
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

    @Override
    public List<AuditOperation> findAllForExport(AuditOperationCriteria criteria) {
        Specification<AuditOperationEntity> spec = buildSpecification(criteria);
        return auditOperationRepository.findAll(spec)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
