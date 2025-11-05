package com.audit.infrastructure.adapters.output.jpa.entity;

import java.time.ZonedDateTime;

import com.audit.domain.enums.UserAction;
import com.audit.domain.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 50)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private UserAction action;

    @Column(name = "action_at", nullable = false)
    private ZonedDateTime actionAt;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

}
