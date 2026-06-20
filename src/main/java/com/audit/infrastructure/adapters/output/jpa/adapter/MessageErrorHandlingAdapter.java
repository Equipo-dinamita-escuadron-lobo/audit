package com.audit.infrastructure.adapters.output.jpa.adapter;

import java.time.Instant;

import org.springframework.stereotype.Repository;

import com.audit.application.port.output.IMessageErrorHandlingPort;
import com.audit.infrastructure.adapters.output.jpa.entity.MessageErrorEntity;
import com.audit.infrastructure.adapters.output.jpa.repository.IMessageErrorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageErrorHandlingAdapter implements IMessageErrorHandlingPort {

    private final IMessageErrorRepository messageRepository;

    @Override
    public void saveProcessingError(String eventType, String errorDescription, String messageData, String entityType,
            String errorStage) {
        try {
            MessageErrorEntity error = MessageErrorEntity.builder()
                    .eventType(eventType)
                    .entityType(entityType)
                    .errorDescription(errorDescription)
                    .messageData(messageData)
                    .errorAt(Instant.now())
                    .errorStage(errorStage)
                    .build();

            messageRepository.save(error);
            log.info("Error saved for entity type: {}, event type: {}", entityType, eventType);

        } catch (Exception e) {
            log.error("Failed to save error to database: {}", e.getMessage());
        }
    }

}
