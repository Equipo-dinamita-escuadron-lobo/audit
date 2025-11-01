package com.audit.infrastructure.adapters.input.messageBroker.mapper;

import org.springframework.stereotype.Component;

import com.audit.application.dto.request.LogSessionRequest;
import com.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;

@Component
public class SessionEventMapper {

    public LogSessionRequest toRequest(SessionEventDto eventDto) {
        return LogSessionRequest.builder()
            .userId(eventDto.getUserId())
            .userName(eventDto.getUserName())
            .userRole(eventDto.getUserRole())
            .action(eventDto.getAction())
            .actionAt(eventDto.getActionAt())
            .ipAddress(eventDto.getIpAddress())
            .build();
    }

}
