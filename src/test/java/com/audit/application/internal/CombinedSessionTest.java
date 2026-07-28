package com.audit.application.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.exceptions.InvalidAuditEventException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombinedSessionTest {

    @Test
    @DisplayName("of - sesión combinada válida con login y logout")
    void of_validCompletedSession() {
        Instant login = Instant.now().minusSeconds(3600);
        Instant logout = Instant.now();

        CombinedSession session = CombinedSession.of(
                "SESSION-001",
                "Freider",
                List.of("ADMIN"),
                login,
                logout);

        assertAll(
                () -> assertEquals("SESSION-001", session.getSessionId()),
                () -> assertEquals("Freider", session.getUserName()),
                () -> assertEquals(List.of("ADMIN"), session.getUserRole()),
                () -> assertEquals(login, session.getLoginTime()),
                () -> assertEquals(logout, session.getLogoutTime()),
                () -> assertFalse(session.isActive()));
    }

    @Test
    @DisplayName("of - sesión sin logout debe considerarse activa")
    void of_withoutLogout_shouldBeActive() {
        Instant login = Instant.now();

        CombinedSession session = CombinedSession.of(
                "SESSION-001",
                "Freider",
                List.of("ADMIN"),
                login,
                null);

        assertTrue(session.isActive());
    }

    @Test
    @DisplayName("of - debe fallar si sessionId es vacío")
    void of_blankSessionId_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> CombinedSession.of(
                " ",
                "Freider",
                List.of("ADMIN"),
                Instant.now(),
                null));
    }

    @Test
    @DisplayName("of - debe fallar si loginTime es nulo")
    void of_nullLoginTime_throwsException() {
        assertThrows(InvalidAuditEventException.class, () -> CombinedSession.of(
                "SESSION-001",
                "Freider",
                List.of("ADMIN"),
                null,
                null));
    }
}
