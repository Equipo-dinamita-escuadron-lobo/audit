package com.audit.domain.port.output;

import java.util.Optional;

import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;
import com.audit.domain.model.CombinedSession;
import com.audit.domain.model.PageResult;

/**
 * @brief Outbound port for session audit persistence
 *        Define the contract that the persistence adapter must implement
 */
public interface AuditSessionRepositoryPort {

    AuditSession save(AuditSession auditSession);

    PageResult<AuditSession> findPageByFilters(AuditSessionFilter filter);

    PageResult<CombinedSession> findCombinedSessions(AuditSessionFilter filter);

    Optional<AuditSession> findById(Long id);

    Optional<AuditSession> findBySessionId(String sessionId);

    boolean existsActiveSession(String userId);
}
