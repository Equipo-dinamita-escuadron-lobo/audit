package com.audit.application.port.output;

import java.util.List;

import com.audit.application.internal.DocumentSummaryProjection;
import com.audit.application.internal.query.AuditDocumentCriteria;
import com.audit.application.internal.query.AuditDocumentExportCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;
import com.audit.domain.model.AuditDocumentEvent;

public interface AuditDocumentEventQueryPort {

        PageResult<DocumentSummaryProjection> findDocumentSummaries(
                        AuditDocumentCriteria criteria, QueryOptions options);

        List<AuditDocumentEvent> findEventsByDocumentCode(
                        String enterpriseId, String documentCode);

        List<DocumentSummaryProjection> findAllDocumentSummariesForExport(AuditDocumentCriteria criteria);

        List<AuditDocumentEvent> findAllEventsForExport(AuditDocumentExportCriteria criteria);

        long countDistinctDocuments(AuditDocumentCriteria criteria);
}
