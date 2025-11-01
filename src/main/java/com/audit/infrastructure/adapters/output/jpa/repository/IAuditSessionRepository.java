package com.audit.infrastructure.adapters.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;

@Repository
public interface IAuditSessionRepository
        extends JpaRepository<AuditSessionEntity, Long>, JpaSpecificationExecutor<AuditSessionEntity> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END" +
            " FROM AuditSessionEntity a" +
            " WHERE a.userId = :userId" +
            " AND a.action = 'LOGIN'" +
            " AND NOT EXISTS (" +
            "     SELECT 1 FROM AuditSessionEntity b" +
            "     WHERE b.userId = :userId" +
            "     AND b.action = 'LOGOUT'" +
            "     AND b.actionAt > a.actionAt" +
            " )")
    boolean existsActiveSession(String userId);

}
