package com.audit.infrastructure.adpaters.input.messageBroker;

import com.audit.infrastructure.adapters.input.messageBroker.AuditOperationListener;
import com.audit.infrastructure.adapters.input.messageBroker.dto.OperationEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.OperationEventMapper;
import com.audit.application.dto.request.LogOperationRequest;
import com.audit.application.port.input.commands.LogAuditOperationCommand;
import com.audit.application.port.output.IMessageErrorHandlingPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuditOperationListenerTest {

        private final LogAuditOperationCommand command = mock(LogAuditOperationCommand.class);
        private final OperationEventMapper mapper = mock(OperationEventMapper.class);
        private final IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Channel channel = mock(Channel.class);

        private final AuditOperationListener listener = new AuditOperationListener(command, mapper, errorPort,
                        objectMapper);

        private OperationEventDto validEvent() {
                return OperationEventDto.builder()
                                .enterpriseId("ENT-001")
                                .userId("USER-001")
                                .userName("Freider")
                                .userRole(List.of("ADMIN"))
                                .operationType("CREATE")
                                .operationAt(Instant.now())
                                .moduleName("CONFIGURATION")
                                .affectedTable("cost_centers")
                                .registerId("1")
                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                .build();
        }

        @Test
        @DisplayName("handleOperationEvent - evento válido debe mapear, ejecutar comando y hacer ACK")
        void handleOperationEvent_validEvent_shouldExecuteCommandAndAck() throws Exception {
                OperationEventDto event = validEvent();
                LogOperationRequest request = mock(LogOperationRequest.class);

                when(mapper.toRequest(event)).thenReturn(request);

                listener.handleOperationEvent(event, channel, 1L);

                verify(mapper).toRequest(event);
                verify(command).execute(request);
                verify(channel).basicAck(1L, false);
                verifyNoInteractions(errorPort);
        }

        @Test
        @DisplayName("handleOperationEvent - enterpriseId vacío debe guardar error de validación")
        void handleOperationEvent_blankEnterpriseId_shouldSaveValidationError() throws Exception {
                OperationEventDto event = OperationEventDto.builder()
                                .enterpriseId(" ")
                                .userId("USER-001")
                                .userName("Freider")
                                .userRole(List.of("ADMIN"))
                                .operationType("CREATE")
                                .operationAt(Instant.now())
                                .moduleName("CONFIGURATION")
                                .affectedTable("cost_centers")
                                .registerId("1")
                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                .build();

                listener.handleOperationEvent(event, channel, 2L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                eq("Validation failed: enterpriseId missing"),
                                anyString(),
                                eq("Operation"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(2L, false);
        }

        @Test
        @DisplayName("handleOperationEvent - dataObject vacío debe guardar error de validación")
        void handleOperationEvent_emptyDataObject_shouldSaveValidationError() throws Exception {
                OperationEventDto event = OperationEventDto.builder()
                                .enterpriseId("ENT-001")
                                .userId("USER-001")
                                .userName("Freider")
                                .userRole(List.of("ADMIN"))
                                .operationType("CREATE")
                                .operationAt(Instant.now())
                                .moduleName("CONFIGURATION")
                                .affectedTable("cost_centers")
                                .registerId("1")
                                .dataObject(Map.of())
                                .build();

                listener.handleOperationEvent(event, channel, 3L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                eq("Validation failed: dataObject missing"),
                                anyString(),
                                eq("Operation"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(3L, false);
        }

        @Test
        @DisplayName("handleOperationEvent - si comando falla debe guardar error y hacer ACK")
        void handleOperationEvent_commandThrows_shouldSaveProcessingError() throws Exception {
                OperationEventDto event = validEvent();
                LogOperationRequest request = mock(LogOperationRequest.class);

                when(mapper.toRequest(event)).thenReturn(request);
                doThrow(new RuntimeException("DB error")).when(command).execute(request);

                listener.handleOperationEvent(event, channel, 4L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                contains("[RuntimeException] DB error"),
                                anyString(),
                                eq("Operation"),
                                eq("VALIDATION"));

                verify(channel).basicAck(4L, false);
        }

        @Test
        @DisplayName("handleOperationEvent - debe validar campos obligatorios faltantes")
        void handleOperationEvent_missingRequiredFields_shouldSaveValidationErrors() throws Exception {
                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId(" ")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .operationAt(Instant.now())
                                                .moduleName("CONFIGURATION")
                                                .affectedTable("cost_centers")
                                                .registerId("1")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                "CREATE",
                                "userId missing");

                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId("USER-001")
                                                .userName(" ")
                                                .userRole(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .operationAt(Instant.now())
                                                .moduleName("CONFIGURATION")
                                                .affectedTable("cost_centers")
                                                .registerId("1")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                "CREATE",
                                "userName missing");

                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of())
                                                .operationType("CREATE")
                                                .operationAt(Instant.now())
                                                .moduleName("CONFIGURATION")
                                                .affectedTable("cost_centers")
                                                .registerId("1")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                "CREATE",
                                "userRole missing");

                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .operationType(" ")
                                                .operationAt(Instant.now())
                                                .moduleName("CONFIGURATION")
                                                .affectedTable("cost_centers")
                                                .registerId("1")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                " ",
                                "operationType missing");

                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .operationAt(null)
                                                .moduleName("CONFIGURATION")
                                                .affectedTable("cost_centers")
                                                .registerId("1")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                "CREATE",
                                "operationAt missing");

                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .operationAt(Instant.now())
                                                .moduleName(" ")
                                                .affectedTable("cost_centers")
                                                .registerId("1")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                "CREATE",
                                "moduleName missing");

                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .operationAt(Instant.now())
                                                .moduleName("CONFIGURATION")
                                                .affectedTable(" ")
                                                .registerId("1")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                "CREATE",
                                "affectedTable missing");

                assertInvalidOperationEvent(
                                OperationEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRole(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .operationAt(Instant.now())
                                                .moduleName("CONFIGURATION")
                                                .affectedTable("cost_centers")
                                                .registerId(" ")
                                                .dataObject(Map.of("entity", Map.of("id", 1L)))
                                                .build(),
                                "CREATE",
                                "registerId missing");
        }

        @Test
        @DisplayName("handleOperationEvent - dataObject demasiado grande debe guardar error de validación")
        void handleOperationEvent_dataObjectTooLarge_shouldSaveValidationError() throws Exception {
                String largeText = "x".repeat(1_000_001);

                OperationEventDto event = OperationEventDto.builder()
                                .enterpriseId("ENT-001")
                                .userId("USER-001")
                                .userName("Freider")
                                .userRole(List.of("ADMIN"))
                                .operationType("CREATE")
                                .operationAt(Instant.now())
                                .moduleName("CONFIGURATION")
                                .affectedTable("cost_centers")
                                .registerId("1")
                                .dataObject(Map.of("payload", largeText))
                                .build();

                listener.handleOperationEvent(event, channel, 55L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                eq("Validation failed: dataObject exceeds max size"),
                                anyString(),
                                eq("Operation"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(55L, false);
        }

        @Test
        @DisplayName("handleOperationEvent - si falla serialización del evento debe guardar JSON de error")
        void handleOperationEvent_jsonSerializationFails_shouldSaveFallbackJson() throws Exception {
                ObjectMapper failingMapper = mock(ObjectMapper.class);

                when(failingMapper.writeValueAsString(any()))
                                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {
                                });

                AuditOperationListener failingListener = new AuditOperationListener(command, mapper,
                                errorPort, failingMapper);

                failingListener.handleOperationEvent(null, channel, 88L);

                verify(errorPort).saveProcessingError(
                                eq("UNKNOWN"),
                                eq("Validation failed: event is null"),
                                eq("{\"error\": \"Failed to convert\"}"),
                                eq("Operation"),
                                eq("VALIDATION"));

                verify(channel).basicAck(88L, false);
        }

        private void assertInvalidOperationEvent(OperationEventDto event, String expectedEventType,
                        String expectedMessage)
                        throws Exception {

                listener.handleOperationEvent(event, channel, 99L);

                verify(errorPort).saveProcessingError(
                                eq(expectedEventType),
                                eq("Validation failed: " + expectedMessage),
                                anyString(),
                                eq("Operation"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(99L, false);

                clearInvocations(errorPort, mapper, command, channel);
        }
}
