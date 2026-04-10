package com.audit.domain.port.output;

import java.util.List;
import java.util.Optional;

import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.PageResult;
import com.audit.application.internal.QueryOptions;
import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionCriteria;

/**
 * @brief Outbound port for session audit persistence
 *        Define the contract that the persistence adapter must implement
 */
public interface AuditSessionRepositoryPort {

    AuditSession save(AuditSession auditSession);

    Optional<AuditSession> findBySessionId(String sessionId);

    long countByCriteria(AuditSessionCriteria criteria);

    PageResult<CombinedSession> findCombinedSessions(AuditSessionCriteria criteria, QueryOptions options);

    List<CombinedSession> findForExport(AuditSessionCriteria criteria);
}
