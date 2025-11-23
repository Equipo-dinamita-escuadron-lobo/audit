package com.audit.infrastructure.adapters.output.jpa.adapter;

import com.audit.domain.enums.UserRole;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.AuditOperationFilter;
import com.audit.domain.model.PageResult;
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
    public PageResult<AuditOperation> findPageByFilters(AuditOperationFilter filter) {
        Specification<AuditOperationEntity> spec = buildSpecification(filter);
        if (filter.hasPagination()) {
            Sort sort = buildSort(filter);
            PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), sort);

            Page<AuditOperationEntity> page = auditOperationRepository.findAll(spec, pageRequest);
            List<AuditOperation> content = page.getContent()
                    .stream()
                    .map(mapper::toDomain)
                    .toList();
            return new PageResult<>(content, page.getTotalElements());
        }
        // Para cuandose quiera exportar
        Sort sort = buildSort(filter);
        List<AuditOperation> all = auditOperationRepository.findAll(spec, sort)
                .stream()
                .map(mapper::toDomain)
                .toList();
        return new PageResult<>(all, all.size());
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
    private Specification<AuditOperationEntity> buildSpecification(AuditOperationFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getEnterpriseId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("enterpriseId"), filter.getEnterpriseId()));
            }
            
            if (filter.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("operationAt"), filter.getDateFrom().toInstant()));
            }

            if (filter.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("operationAt"), filter.getDateTo().toInstant()));
            }

            if (filter.hasModuleNameFilter()) {
                String searchTerm = filter.getModuleName().toLowerCase().trim();
                if (searchTerm.length() < 3) {
                    throw new IllegalArgumentException("moduleName filter must be at least 3 characters");
                }
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("moduleName")),
                        searchTerm + "%"));
            }

            if (filter.hasAffectedTableFilter()) {
                String searchTerm = filter.getAffectedTable().toLowerCase().trim();
                if (searchTerm.length() < 3) {
                    throw new IllegalArgumentException("affectedTable filter must be at least 3 characters");
                }
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("affectedTable")),
                        searchTerm + "%"));
            }

            if (filter.hasUserNameFilter()) {
                String searchTerm = filter.getUserName().toLowerCase().trim();
                if (searchTerm.length() < 3) {
                    throw new IllegalArgumentException("userName filter must be at least 3 characters");
                }
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("userName")),
                        searchTerm + "%"));
            }

            if (filter.hasUserRoleFilter()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("userRole"), filter.getUserRole()));
            }

            if (filter.hasOperationTypeFilter()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("operationType"), filter.getOperationType()));
            }

            if (filter.hasRegisterIdFilter()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("registerId"), filter.getRegisterId()));
            }

            if (filter.getRequestingUserRole() != null) {
                String role = filter.getRequestingUserRole().toUpperCase();
                if ("PROFESOR".equals(role) || "ESTUDIANTE".equals(role)) {
                    predicates.add(criteriaBuilder.notEqual(root.get("userRole"), UserRole.ADMINISTRADOR));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(AuditOperationFilter filter) {
        String sortField = filter.getSortField() != null ? filter.getSortField() : "operationAt";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

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
}
