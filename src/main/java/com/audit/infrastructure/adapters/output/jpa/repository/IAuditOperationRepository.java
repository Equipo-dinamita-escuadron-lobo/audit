package com.audit.infrastructure.adapters.output.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.audit.infrastructure.adapters.output.jpa.entity.AuditOperationEntity;

@Repository
public interface IAuditOperationRepository extends JpaRepository<AuditOperationEntity, Long>,
        JpaSpecificationExecutor<AuditOperationEntity> {

    /**
    * Busca operaciones de un registro específico en una tabla
    */
    Optional<AuditOperationEntity> findByRegisterIdAndAffectedTable(
        String registerId, String affectedTable
    );

}
