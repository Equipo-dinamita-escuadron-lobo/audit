package com.audit.infrastructure.adapters.input.messageBroker.dto;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SessionEventDto {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_role")
    private UserRole userRole;

    @JsonProperty("action")
    private UserAction action;

    @JsonProperty("action_at")
    private ZonedDateTime actionAt;

    @JsonProperty("ip_address")
    private String ipAddress;

}
