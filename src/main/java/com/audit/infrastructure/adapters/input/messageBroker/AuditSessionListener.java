package com.audit.infrastructure.adapters.input.messageBroker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import com.audit.application.dto.request.LogSessionRequest;
import com.audit.application.port.input.commands.LogAuditSessionCommand;
import com.audit.domain.port.messageProcessingError.IMessageErrorHandlingPort;
import com.audit.infrastructure.adapters.input.messageBroker.base.AbstractMessageListener;
import com.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.SessionEventMapper;
import com.audit.infrastructure.config.rabbitConfig.RabbitSessionConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;

import org.springframework.messaging.handler.annotation.Header;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditSessionListener extends AbstractMessageListener<SessionEventDto> {

    private final LogAuditSessionCommand logAuditSessionPort;
    private final SessionEventMapper sessionEventMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;

    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
    }

    @RabbitListener(queues = RabbitSessionConfig.SESSION_AUDIT_QUEUE)
    public void handleSessionEvent(
        SessionEventDto eventDto,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        handleMessage(eventDto, channel, deliveryTag);
    }

    @Override
    protected void processEvent(SessionEventDto event) {
        LogSessionRequest request = sessionEventMapper.toRequest(event);
        logAuditSessionPort.executeAsync(request);
    }


    @Override
    protected boolean isValidEvent(SessionEventDto event) {
        return event != null
            && event.getSessionId() != null && !event.getSessionId().trim().isEmpty()
            && event.getUserId() != null && !event.getUserId().trim().isEmpty()
            && event.getUserName() != null && !event.getUserName().trim().isEmpty()
            && event.getAction() != null 
            && event.getActionAt() != null;
    }


    @Override
    protected String getEntityType() {
        return "Session";
    }


    @Override
    protected String extractEventType(SessionEventDto event) {
        return event != null && event.getAction() != null ? event.getAction().name() : null;
    }


    @Override
    protected String convertEventToJson(SessionEventDto event) {
        try {
            return new ObjectMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to convert\"}";
        }
    }

}
