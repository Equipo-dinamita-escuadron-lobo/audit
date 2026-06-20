package com.audit.domain.port.output;

import java.util.Optional;
import com.audit.domain.model.AuditSession;

/**
 * @brief Outbound port for session audit persistence
 *        Define the contract that the persistence adapter must implement
 */
public interface AuditSessionRepositoryPort {

    AuditSession save(AuditSession auditSession);

    Optional<AuditSession> findBySessionId(String sessionId);
}
