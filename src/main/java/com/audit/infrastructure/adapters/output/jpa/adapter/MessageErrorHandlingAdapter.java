package com.audit.infrastructure.adapters.output.jpa.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.audit.application.dto.response.MessageErrorResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.output.IMessageErrorHandlingPort;
import com.audit.application.port.output.MessageErrorQueryPort;
import com.audit.infrastructure.adapters.output.jpa.entity.MessageErrorEntity;
import com.audit.infrastructure.adapters.output.jpa.repository.IMessageErrorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageErrorHandlingAdapter implements IMessageErrorHandlingPort, MessageErrorQueryPort {

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

    @Override
    public Optional<MessageErrorResponse> findLatest() {
        return messageRepository.findFirstByOrderByErrorAtDesc()
                .map(this::toResponse);
    }

    @Override
    public PageResponse<MessageErrorResponse> findAll(int page, int size) {
        Page<MessageErrorEntity> result = messageRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "errorAt")));

        List<MessageErrorResponse> data = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<MessageErrorResponse>builder()
                .data(data)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();
    }

    @Override
    public void deleteAll() {
        messageRepository.deleteAll();
    }

    private MessageErrorResponse toResponse(MessageErrorEntity entity) {
        return MessageErrorResponse.builder()
                .id(entity.getId())
                .eventType(entity.getEventType())
                .entityType(entity.getEntityType())
                .errorDescription(entity.getErrorDescription())
                .messageData(entity.getMessageData())
                .errorStage(entity.getErrorStage())
                .errorAt(entity.getErrorAt())
                .build();
    }

}
