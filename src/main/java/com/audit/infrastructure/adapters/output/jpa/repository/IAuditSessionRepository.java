package com.audit.infrastructure.adapters.output.jpa.repository;

import java.time.ZonedDateTime;
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
        extends JpaRepository<AuditSessionEntity, Long>, JpaSpecificationExecutor<AuditSessionEntity> {

        @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
                "FROM AuditSessionEntity a " +
                "WHERE a.userId = :userId " +
                "AND a.action = 'LOGIN' " +
                "AND NOT EXISTS (" +
                "    SELECT 1 FROM AuditSessionEntity b " +
                "    WHERE b.sessionId = a.sessionId " + 
                "    AND b.action = 'LOGOUT'" +
                ")")
        boolean existsActiveSession(String userId);

        Optional<AuditSessionEntity> findBySessionId(String sessionId);

        @Query(value = """
        SELECT 
            a.session_id as sessionId,
            a.user_name as userName,
            a.user_role as userRole,
            MIN(CASE WHEN a.action = 'LOGIN' THEN a.action_at END) as loginTime,
            MAX(CASE WHEN a.action = 'LOGOUT' THEN a.action_at END) as logoutTime
        FROM audit_session a
        WHERE 
            (:userName IS NULL OR LOWER(a.user_name) LIKE LOWER(CONCAT('%', :userName, '%')))
            AND (:userRole IS NULL OR a.user_role = CAST(:userRole AS text))
            AND a.action_at >= :dateFrom
            AND a.action_at <= :dateTo
            AND (:requestingRole != 'DOCENTE' OR a.user_role != 'ADMIN')
        GROUP BY a.session_id, a.user_name, a.user_role
        ORDER BY loginTime DESC
        """, 
        countQuery = """
        SELECT COUNT(DISTINCT a.session_id)
        FROM audit_session a
        WHERE 
            (:userName IS NULL OR LOWER(a.user_name) LIKE LOWER(CONCAT('%', :userName, '%')))
            AND (:userRole IS NULL OR a.user_role = CAST(:userRole AS text))
            AND a.action_at >= :dateFrom
            AND a.action_at <= :dateTo
            AND (:requestingRole != 'DOCENTE' OR a.user_role != 'ADMIN')
        """,
        nativeQuery = true)
        Page<SessionProjection> findCombinedSessions(
                @Param("dateFrom") ZonedDateTime dateFrom,
                @Param("dateTo") ZonedDateTime dateTo,
                @Param("userName") String userName,
                @Param("userRole") String userRole,
                @Param("requestingRole") String requestingRole,
                Pageable pageable
        );

}
