package com.audit.infrastructure.adapters.output.jpa.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message_processing_errors")
@Builder
public class MessageErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "error_description", columnDefinition = "TEXT")
    private String errorDescription;

    @Column(name = "message_data", columnDefinition = "TEXT")
    private String messageData;

    @Column(name = "error_stage")
    private String errorStage;

    @Column(name = "error_at", nullable = false)
    private Instant errorAt;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    /**
     * @brief Sets error timestamp before entity persistence
     */
    @PrePersist
    protected void onCreate() {
        if (errorAt == null) {
            errorAt = Instant.now();
        }
    }

}
