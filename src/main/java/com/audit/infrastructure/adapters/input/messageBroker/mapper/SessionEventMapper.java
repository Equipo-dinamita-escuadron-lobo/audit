package com.audit.infrastructure.adapters.input.messageBroker.mapper;


import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.audit.application.dto.request.LogSessionRequest;
import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;
import com.audit.domain.exceptions.InvalidAuditEventException;
import com.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;

@Component
public class SessionEventMapper {

    public LogSessionRequest toRequest(SessionEventDto eventDto) {
        return LogSessionRequest.builder()
                .sessionId(sanitizeText(eventDto.getSessionId()))
                .userId(sanitizeText(eventDto.getUserId()))
                .userName(sanitizeText(eventDto.getUserName()))
                .userRole(parseUserRole(eventDto.getUserRole()))
                .action(parseUserAction(eventDto.getAction()))
                .actionAt(eventDto.getActionAt().atZone(ZoneId.of("UTC")))
                .ipAddress(eventDto.getIpAddress())
                .build();
    }
    
    private UserRole parseUserRole(String role) {
        if (role == null) {
            throw new InvalidAuditEventException("User role cannot be null");
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidAuditEventException("Invalid user role received: " + role);
        }
    }

    private UserAction parseUserAction(String action) {
        if (action == null) {
            throw new InvalidAuditEventException("User action cannot be null");
        }
        try {
            return UserAction.valueOf(action.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidAuditEventException("Invalid user action received: " + action);
        }
    }

    private String sanitizeText(String input) {
        if (input == null)
            return null;
        String sanitized = input.trim()
                .replaceAll("[\\n\\r\\t]", " ")
                .replaceAll("[<>]", "");
        return sanitized;
    }

}
