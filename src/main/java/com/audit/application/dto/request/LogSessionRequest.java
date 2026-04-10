package com.audit.application.dto.request;

import java.time.Instant;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;

/**
 * @brief DTO for session event registration requests
 *        Used for communication between primary adapters and the application
 */

@Getter
@Builder
public class LogSessionRequest {
    private String sessionId;
    private String userId;
    private String userName;
    private UserRole userRole;
    private UserAction action;
    private Instant actionAt;
    private String ipAddress;
}
