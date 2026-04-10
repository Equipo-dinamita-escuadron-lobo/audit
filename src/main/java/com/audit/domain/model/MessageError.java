package com.audit.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageError {

    private Long id;
    private String eventType;
    private String errorDescription;
    private String messageData;
    private String entityType;
    private Instant errorAt;
    private String errorStage;
}
