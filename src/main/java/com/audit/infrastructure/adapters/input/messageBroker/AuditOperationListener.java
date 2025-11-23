package com.audit.infrastructure.adapters.input.messageBroker;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.audit.application.dto.request.LogOperationRequest;
import com.audit.application.port.input.commands.LogAuditOperationCommand;
import com.audit.domain.port.messageProcessingError.IMessageErrorHandlingPort;
import com.audit.infrastructure.adapters.input.messageBroker.base.AbstractMessageListener;
import com.audit.infrastructure.adapters.input.messageBroker.dto.OperationEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.OperationEventMapper;
import com.audit.infrastructure.config.rabbitConfig.RabbitOperationConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditOperationListener extends AbstractMessageListener<OperationEventDto> {

    private final LogAuditOperationCommand logOperationPort;
    private final OperationEventMapper operationEventMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;

    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
    }

    @RabbitListener(queues = RabbitOperationConfig.OPERATION_AUDIT_QUEUE)
    public void handleOperationEvent(
            OperationEventDto eventDto,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handleMessage(eventDto, channel, deliveryTag);
    }

    @Override
    protected void processEvent(OperationEventDto event) {
        LogOperationRequest request = operationEventMapper.toRequest(event);
        logOperationPort.execute(request);
    }

    @Override
    protected boolean isValidEvent(OperationEventDto event) {
        return event != null
                && event.getEnterpriseId() != null && !event.getEnterpriseId().trim().isEmpty()
                && event.getUserId() != null && !event.getUserId().trim().isEmpty()
                && event.getUserName() != null && !event.getUserName().trim().isEmpty()
                && event.getUserRole() != null && !event.getUserRole().trim().isEmpty()
                && event.getOperationType() != null && !event.getOperationType().trim().isEmpty()
                && event.getOperationAt() != null
                && event.getModuleName() != null && !event.getModuleName().trim().isEmpty()
                && event.getAffectedTable() != null && !event.getAffectedTable().trim().isEmpty()
                && event.getRegisterId() != null && !event.getRegisterId().trim().isEmpty()
                && event.getDataObject() != null && !event.getDataObject().isEmpty()
                && isValidDataObjectSize(event.getDataObject());
    }

    private boolean isValidDataObjectSize(Map<String, Object> dataObject) {
        try {
            String json = new ObjectMapper().writeValueAsString(dataObject);
            return json.length() <= 1_000_000;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getEntityType() {
        return "Operation";
    }

    @Override
    protected String extractEventType(OperationEventDto event) {
        return event != null && event.getOperationType() != null ? event.getOperationType() : null;
    }

    @Override
    protected String convertEventToJson(OperationEventDto event) {
        try {
            return new ObjectMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to convert\"}";
        }
    }

}
