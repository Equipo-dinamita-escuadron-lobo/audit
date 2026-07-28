package com.audit.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.UserAction;
import com.audit.domain.exceptions.InvalidAuditEventException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditSessionTest {

        @Test
        @DisplayName("create - sesión válida debe crear auditoría de sesión")
        void create_validSession() {
                AuditSession session = AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                "127.0.0.1");

                assertAll(
                                () -> assertNull(session.getId()),
                                () -> assertEquals("SESSION-001", session.getSessionId()),
                                () -> assertEquals("USER-001", session.getUserId()),
                                () -> assertEquals("Freider", session.getUserName()),
                                () -> assertEquals(List.of("ADMIN"), session.getUserRole()),
                                () -> assertEquals(UserAction.LOGIN, session.getAction()),
                                () -> assertEquals("127.0.0.1", session.getIpAddress()),
                                () -> assertNotNull(session.getCreatedAt()));
        }

        @Test
        @DisplayName("create - debe fallar si el userId es vacío")
        void create_emptyUserId_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                " ",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("create - debe fallar si los roles están vacíos")
        void create_emptyRoles_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                "Freider",
                                List.of(),
                                UserAction.LOGIN,
                                Instant.now(),
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("create - debe fallar si la fecha de acción viene muy al futuro")
        void create_futureActionAt_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now().plusSeconds(301),
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("create - debe fallar si sessionId es nulo")
        void create_nullSessionId_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                null,
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("create - debe fallar si sessionId está vacío")
        void create_emptySessionId_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                " ",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("create - debe fallar si userName es nulo")
        void create_nullUserName_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                null,
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("create - debe fallar si userName está vacío")
        void create_emptyUserName_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                " ",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("create - debe fallar si ipAddress es nulo")
        void create_nullIpAddress_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                null));
        }

        @Test
        @DisplayName("create - debe fallar si ipAddress está vacío")
        void create_emptyIpAddress_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                Instant.now(),
                                " "));
        }

        @Test
        @DisplayName("create - debe fallar si actionAt es nulo")
        void create_nullActionAt_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditSession.create(
                                "SESSION-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGIN,
                                null,
                                "127.0.0.1"));
        }

        @Test
        @DisplayName("reconstruct - debe reconstruir sesión existente sin validar")
        void reconstruct_existingSession() {
                Instant now = Instant.now();

                AuditSession session = AuditSession.reconstruct(
                                1L,
                                "SESSION-001",
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                UserAction.LOGOUT,
                                now,
                                "127.0.0.1",
                                now);

                assertAll(
                                () -> assertEquals(1L, session.getId()),
                                () -> assertEquals(UserAction.LOGOUT, session.getAction()),
                                () -> assertEquals(now, session.getCreatedAt()));
        }
}
