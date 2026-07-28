package com.audit.application.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageErrorResponse {
    private Long id;
    private String eventType;
    private String entityType;
    private String errorDescription;
    private String messageData;
    private String errorStage;
    private Instant errorAt;
}
