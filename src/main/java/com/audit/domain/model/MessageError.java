package com.audit.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageError {

    private Long id;
    private String eventType;
    private String errorDescription;
    private String messageData;
    private String entityType;
    private Instant errorAt;
}
