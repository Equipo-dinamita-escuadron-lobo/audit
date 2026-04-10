package com.audit.application.dto.response;

import java.time.Instant;

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
    private String userName;
    private String userRole;
    private Instant loginTime;
    private Instant logoutTime;
}
