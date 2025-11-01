package com.audit.domain.port.output;

import java.util.List;
import java.util.Optional;

import com.audit.domain.model.AuditSession;
import com.audit.domain.model.AuditSessionFilter;

/**
 * @brief Outbound port for session audit persistence
 *        Define the contract that the persistence adapter must implement
 */
public interface AuditSessionRepositoryPort {
    AuditSession save(AuditSession auditSession);

    List<AuditSession> findByFilters(AuditSessionFilter filter);

    Optional<AuditSession> findById(Long id);

    long countByFilters(AuditSessionFilter filter);

    boolean existsActiveSession(String userId);
}
