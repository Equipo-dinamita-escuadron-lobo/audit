package com.audit.infrastructure.adpaters.input.messageBroker;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.dto.request.LogSessionRequest;
import com.audit.domain.enums.UserAction;
import com.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.SessionEventMapper;

class SessionEventMapperTest {

    private final SessionEventMapper mapper = new SessionEventMapper();

    private SessionEventDto validDto() {
        return SessionEventDto.builder()
                .sessionId("  sess-001  ")
                .userId("USER-001")
                .userName("Juan\t")
                .userRole(List.of("ADMIN"))
                .action("LOGIN")
                .actionAt(Instant.now())
                .ipAddress("192.168.1.1")
                .build();
    }

    @Test
    @DisplayName("toRequest - debe sanitizar tabs y espacios del userName")
    void toRequest_validDto_shouldMapAndSanitize() {
        LogSessionRequest result = mapper.toRequest(validDto());

        assertAll(
                () -> assertEquals("sess-001", result.getSessionId()),
                () -> assertEquals("USER-001", result.getUserId()),
                () -> assertFalse(result.getUserName().contains("\t"), "userName no debe contener tabs"),
                () -> assertFalse(result.getUserName().contains("\n"), "userName no debe contener saltos de línea"),
                () -> assertEquals(List.of("ADMIN"), result.getUserRole()),
                () -> assertEquals(UserAction.LOGIN, result.getAction()),
                () -> assertEquals("192.168.1.1", result.getIpAddress()));
    }

    @Test
    @DisplayName("toRequest - action LOGOUT debe parsearse correctamente")
    void toRequest_logoutAction_shouldParse() {
        SessionEventDto dto = validDto();
        dto.setAction("logout");

        LogSessionRequest result = mapper.toRequest(dto);

        assertEquals(UserAction.LOGOUT, result.getAction());
    }

    @Test
    @DisplayName("toRequest - action inválida debe lanzar RuntimeException")
    void toRequest_invalidAction_shouldThrow() {
        SessionEventDto dto = validDto();
        dto.setAction("INVALID_ACTION");

        assertThrows(RuntimeException.class, () -> mapper.toRequest(dto));
    }

    @Test
    @DisplayName("toRequest - action null debe lanzar RuntimeException")
    void toRequest_nullAction_shouldThrow() {
        SessionEventDto dto = validDto();
        dto.setAction(null);

        assertThrows(RuntimeException.class, () -> mapper.toRequest(dto));
    }

    @Test
    @DisplayName("toRequest - userId null no debe lanzar excepción y queda null")
    void toRequest_nullUserId_shouldRemainNull() {
        SessionEventDto dto = validDto();
        dto.setUserId(null);

        LogSessionRequest result = mapper.toRequest(dto);

        assertNull(result.getUserId());
    }
}
