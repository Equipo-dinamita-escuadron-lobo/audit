package com.audit.application.usecases.queries;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audit.application.dto.request.GetDocumentEventDetailRequest;
import com.audit.application.dto.response.DocumentEventDetailResponse;
import com.audit.application.port.input.queries.GetAuditDocumentEventDetailsQuery;
import com.audit.application.port.output.AuditDocumentEventQueryPort;
import com.audit.domain.model.AuditDocumentEvent;

@Service
public class GetAuditDocumentEventDetailsQueryImpl implements GetAuditDocumentEventDetailsQuery {

    private final AuditDocumentEventQueryPort queryPort;

    public GetAuditDocumentEventDetailsQueryImpl(AuditDocumentEventQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentEventDetailResponse> execute(GetDocumentEventDetailRequest request) {
        List<AuditDocumentEvent> events = queryPort.findEventsByDocumentCode(
                request.getEnterpriseId(), request.getDocumentCode());

        return events.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    private DocumentEventDetailResponse toDetailResponse(AuditDocumentEvent event) {
        return DocumentEventDetailResponse.builder()
                .operationType(event.getOperationType().name())
                .userName(event.getUserName())
                .userRoles(event.getUserRoles())
                .operationAt(event.getOperationAt())
                .documentData(event.getDocumentData())
                .build();
    }
}
