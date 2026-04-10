package com.audit.infrastructure.adapters.output.jpa.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import com.audit.infrastructure.adapters.output.jpa.projection.SessionProjection;

@Repository
public interface IAuditSessionRepository
        extends JpaRepository<AuditSessionEntity, Long>,
        JpaSpecificationExecutor<AuditSessionEntity> {

    Optional<AuditSessionEntity> findBySessionId(String sessionId);
    @Query(value = """
            SELECT
                login.session_id as sessionId,
                login.user_name as userName,
                login.user_role as userRole,
                login.action_at as loginTime,
                logout.action_at as logoutTime
            FROM audit_session login
            LEFT JOIN audit_session logout
                ON login.session_id = logout.session_id
                AND logout.action = 'LOGOUT'
            WHERE login.action = 'LOGIN'
                AND login.action_at >= :dateFrom
                AND login.action_at <= :dateTo
                AND (:userName IS NULL OR login.user_name ILIKE CONCAT(:userName, '%'))
                AND (:userRole IS NULL OR login.user_role = CAST(:userRole AS text))
                AND (:requestingRole != 'PROFESOR' OR login.user_role != 'ADMINISTRADOR')
            /*#sortBy*/
            """, countQuery = """
            SELECT COUNT(*)
            FROM audit_session
            WHERE action = 'LOGIN'
                AND action_at >= :dateFrom
                AND action_at <= :dateTo
                AND (:userName IS NULL OR user_name ILIKE CONCAT(:userName, '%'))
                AND (:userRole IS NULL OR user_role = CAST(:userRole AS text))
                AND (:requestingRole != 'PROFESOR' OR user_role != 'ADMINISTRADOR')
            """, nativeQuery = true)
    Page<SessionProjection> findCombinedSessions(
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("userName") String userName,
            @Param("userRole") String userRole,
            @Param("requestingRole") String requestingRole,
            Pageable pageable);

    
    @Query(value = """
        SELECT COUNT(*)
        FROM audit_session
        WHERE action = 'LOGIN'
            AND action_at >= :dateFrom
            AND action_at <= :dateTo
            AND (:userName IS NULL OR user_name ILIKE CONCAT(:userName, '%'))
            AND (:userRole IS NULL OR user_role = CAST(:userRole AS text))
            AND (:requestingRole != 'PROFESOR' OR user_role != 'ADMINISTRADOR')
        """, nativeQuery = true)
    long countCombinedSessions(
        @Param("dateFrom") Instant dateFrom,
        @Param("dateTo") Instant dateTo,
        @Param("userName") String userName,
        @Param("userRole") String userRole,
        @Param("requestingRole") String requestingRole);
}
