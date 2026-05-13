package com.audit.application.port.output;

import java.util.List;

import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.query.AuditSessionCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;

public interface AuditSessionQueryPort {

    PageResult<CombinedSession> findCombinedSessions(AuditSessionCriteria criteria, QueryOptions options);

    long countByCriteria(AuditSessionCriteria criteria);

    List<CombinedSession> findAllForExport(AuditSessionCriteria criteria);
}
