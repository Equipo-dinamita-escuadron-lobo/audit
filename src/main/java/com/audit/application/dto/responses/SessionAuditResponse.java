package com.audit.application.dto.responses;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;
import com.audit.domain.model.AuditSession;

import lombok.Builder;
import lombok.Getter;

/**
 * 
 * @brief DTO for session audit event response
 *        Represents the view that is sent to the primary adapters
 */
@Getter
@Builder
public class SessionAuditResponse {
    private Long id;
    private String userId;
    private String userName;
    private UserRole userRole;
    private UserAction action;
    private ZonedDateTime actionAt;
    private String ipAddress;
    private ZonedDateTime createdAt;
    // Calculated fields for UI
    private String actionDisplayName;
    private boolean isLoginEvent;
    private boolean isLogoutEvent;

    /**
     * Factory method to create from domain entity
     */
    public static SessionAuditResponse from(AuditSession session) {
        return SessionAuditResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .userName(session.getUserName())
                .userRole(session.getUserRole())
                .action(session.getAction())
                .actionAt(session.getActionAt())
                .ipAddress(session.getIpAddress())
                .createdAt(session.getCreatedAt())
                .actionDisplayName(session.getAction().name())
                .isLoginEvent(session.isLoginAction())
                .isLogoutEvent(session.isLogoutAction())
                .build();
    }
}
