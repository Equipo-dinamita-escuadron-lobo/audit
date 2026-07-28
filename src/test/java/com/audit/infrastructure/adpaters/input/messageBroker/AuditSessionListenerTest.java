package com.audit.infrastructure.adpaters.input.messageBroker;

import com.audit.application.dto.request.LogSessionRequest;
import com.audit.infrastructure.adapters.input.messageBroker.AuditSessionListener;
import com.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.SessionEventMapper;
import com.audit.application.port.input.commands.LogAuditSessionCommand;
import com.audit.application.port.output.IMessageErrorHandlingPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuditSessionListenerTest {

        private final LogAuditSessionCommand command = mock(LogAuditSessionCommand.class);
        private final SessionEventMapper mapper = mock(SessionEventMapper.class);
        private final IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Channel channel = mock(Channel.class);

        private final AuditSessionListener listener = new AuditSessionListener(command, mapper, errorPort,
                        objectMapper);

        private SessionEventDto validEvent() {
                return SessionEventDto.builder()
                                .sessionId("SESSION-001")
                                .userId("USER-001")
                                .userName("Freider")
                                .userRole(List.of("ADMIN"))
                                .action("LOGIN")
                                .actionAt(Instant.now())
                                .build();
        }

        @Test
        @DisplayName("handleSessionEvent - evento válido debe mapear, ejecutar comando y hacer ACK")
        void handleSessionEvent_validEvent_shouldExecuteCommandAndAck() throws Exception {
                SessionEventDto event = validEvent();
                LogSessionRequest request = mock(LogSessionRequest.class);

                when(mapper.toRequest(event)).thenReturn(request);

                listener.handleSessionEvent(event, channel, 1L);

                verify(mapper).toRequest(event);
                verify(command).execute(request);
                verify(channel).basicAck(1L, false);
                verifyNoInteractions(errorPort);
        }

        @Test
        @DisplayName("handleSessionEvent - evento nulo debe guardar error y no ejecutar comando")
        void handleSessionEvent_nullEvent_shouldSaveValidationError() throws Exception {
                listener.handleSessionEvent(null, channel, 2L);

                verify(errorPort).saveProcessingError(
                                eq("UNKNOWN"),
                                eq("Validation failed: event is null"),
                                anyString(),
                                eq("Session"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(2L, false);
        }

        @Test
        @DisplayName("handleSessionEvent - userId vacío debe guardar error de validación")
        void handleSessionEvent_blankUserId_shouldSaveValidationError() throws Exception {
                SessionEventDto event = validEvent();
                event.setUserId(" ");

                listener.handleSessionEvent(event, channel, 3L);

                verify(errorPort).saveProcessingError(
                                eq("LOGIN"),
                                eq("Validation failed: userId missing"),
                                anyString(),
                                eq("Session"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(3L, false);
        }

        @Test
        @DisplayName("handleSessionEvent - si comando falla debe guardar error y hacer ACK")
        void handleSessionEvent_commandThrows_shouldSaveProcessingError() throws Exception {
                SessionEventDto event = validEvent();
                LogSessionRequest request = mock(LogSessionRequest.class);

                when(mapper.toRequest(event)).thenReturn(request);
                doThrow(new RuntimeException("DB error")).when(command).execute(request);

                listener.handleSessionEvent(event, channel, 4L);

                verify(errorPort).saveProcessingError(
                                eq("LOGIN"),
                                contains("[RuntimeException] DB error"),
                                anyString(),
                                eq("Session"),
                                eq("VALIDATION"));

                verify(channel).basicAck(4L, false);
        }

        @Test
        @DisplayName("handleSessionEvent - debe validar campos obligatorios faltantes")
        void handleSessionEvent_missingRequiredFields_shouldSaveValidationErrors() throws Exception {
                assertInvalidSessionEvent(
                                SessionEventDto.builder()
                                                .sessionId(" ")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .action("LOGIN")
                                                .actionAt(Instant.now())
                                                .build(),
                                "LOGIN",
                                "sessionId missing");

                assertInvalidSessionEvent(
                                SessionEventDto.builder()
                                                .sessionId("SESSION-001")
                                                .userId("USER-001")
                                                .userName(" ")
                                                .userRole(List.of("ADMIN"))
                                                .action("LOGIN")
                                                .actionAt(Instant.now())
                                                .build(),
                                "LOGIN",
                                "userName missing");

                assertInvalidSessionEvent(
                                SessionEventDto.builder()
                                                .sessionId("SESSION-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of())
                                                .action("LOGIN")
                                                .actionAt(Instant.now())
                                                .build(),
                                "LOGIN",
                                "userRole missing");

                assertInvalidSessionEvent(
                                SessionEventDto.builder()
                                                .sessionId("SESSION-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .action(null)
                                                .actionAt(Instant.now())
                                                .build(),
                                "UNKNOWN",
                                "action missing");

                assertInvalidSessionEvent(
                                SessionEventDto.builder()
                                                .sessionId("SESSION-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .action("LOGIN")
                                                .actionAt(null)
                                                .build(),
                                "LOGIN",
                                "actionAt missing");
        }

        @Test
        @DisplayName("handleSessionEvent - si falla serialización del evento debe guardar JSON de error")
        void handleSessionEvent_jsonSerializationFails_shouldSaveFallbackJson() throws Exception {
                ObjectMapper failingMapper = mock(ObjectMapper.class);

                when(failingMapper.writeValueAsString(any()))
                                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {
                                });

                AuditSessionListener failingListener = new AuditSessionListener(command, mapper, errorPort,
                                failingMapper);

                failingListener.handleSessionEvent(null, channel, 88L);

                verify(errorPort).saveProcessingError(
                                eq("UNKNOWN"),
                                eq("Validation failed: event is null"),
                                eq("{\"error\": \"Failed to convert\"}"),
                                eq("Session"),
                                eq("VALIDATION"));

                verify(channel).basicAck(88L, false);
        }

        private void assertInvalidSessionEvent(SessionEventDto event, String expectedEventType, String expectedMessage)
                        throws Exception {

                listener.handleSessionEvent(event, channel, 99L);

                verify(errorPort).saveProcessingError(
                                eq(expectedEventType),
                                eq("Validation failed: " + expectedMessage),
                                anyString(),
                                eq("Session"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(99L, false);

                clearInvocations(errorPort, mapper, command, channel);
        }
}