package com.audit.infrastructure.adpaters.input.messageBroker;

import com.audit.application.dto.request.LogDocumentEventRequest;
import com.audit.application.port.input.commands.LogAuditDocumentEventCommand;
import com.audit.application.port.output.IMessageErrorHandlingPort;
import com.audit.infrastructure.adapters.input.messageBroker.AuditDocumentEventDtoListener;
import com.audit.infrastructure.adapters.input.messageBroker.dto.DocumentEventDto;
import com.audit.infrastructure.adapters.input.messageBroker.mapper.DocumentEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuditDocumentEventDtoListenerTest {

        private final LogAuditDocumentEventCommand command = mock(LogAuditDocumentEventCommand.class);
        private final DocumentEventMapper mapper = mock(DocumentEventMapper.class);
        private final IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Channel channel = mock(Channel.class);

        private final AuditDocumentEventDtoListener listener = new AuditDocumentEventDtoListener(command, mapper,
                        errorPort,
                        objectMapper);

        private DocumentEventDto validEvent() {
                return DocumentEventDto.builder()
                                .enterpriseId("ENT-001")
                                .documentId("DOC-001")
                                .documentCode("FAC-001")
                                .documentType("FACTURA")
                                .documentDate(LocalDate.now())
                                .userId("USER-001")
                                .userName("Freider")
                                .userRoles(List.of("ADMIN"))
                                .operationType("CREATE")
                                .moduleName("DOCUMENTS")
                                .operationAt(Instant.now())
                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                .build();
        }

        @Test
        @DisplayName("handleDocumentEvent - evento válido debe mapear, ejecutar comando y hacer ACK")
        void handleDocumentEvent_validEvent_shouldExecuteCommandAndAck() throws Exception {
                DocumentEventDto event = validEvent();
                LogDocumentEventRequest request = mock(LogDocumentEventRequest.class);

                when(mapper.toRequest(event)).thenReturn(request);

                listener.handleDocumentEvent(event, channel, 1L);

                verify(mapper).toRequest(event);
                verify(command).execute(request);
                verify(channel).basicAck(1L, false);
                verifyNoInteractions(errorPort);
        }

        @Test
        @DisplayName("handleDocumentEvent - documentCode vacío debe guardar error de validación")
        void handleDocumentEvent_blankDocumentCode_shouldSaveValidationError() throws Exception {
                DocumentEventDto event = DocumentEventDto.builder()
                                .enterpriseId("ENT-001")
                                .documentId("DOC-001")
                                .documentCode(" ")
                                .documentType("FACTURA")
                                .documentDate(LocalDate.now())
                                .userId("USER-001")
                                .userName("Freider")
                                .userRoles(List.of("ADMIN"))
                                .operationType("CREATE")
                                .moduleName("DOCUMENTS")
                                .operationAt(Instant.now())
                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                .build();

                listener.handleDocumentEvent(event, channel, 2L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                eq("Validation failed: documentCode missing"),
                                anyString(),
                                eq("Document_event"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(2L, false);
        }

        @Test
        @DisplayName("handleDocumentEvent - documentData vacío debe guardar error de validación")
        void handleDocumentEvent_emptyDocumentData_shouldSaveValidationError() throws Exception {
                DocumentEventDto event = DocumentEventDto.builder()
                                .enterpriseId("ENT-001")
                                .documentId("DOC-001")
                                .documentCode("FAC-001")
                                .documentType("FACTURA")
                                .documentDate(LocalDate.now())
                                .userId("USER-001")
                                .userName("Freider")
                                .userRoles(List.of("ADMIN"))
                                .operationType("CREATE")
                                .moduleName("DOCUMENTS")
                                .operationAt(Instant.now())
                                .documentData(Map.of())
                                .build();

                listener.handleDocumentEvent(event, channel, 3L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                eq("Validation failed: documentData missing"),
                                anyString(),
                                eq("Document_event"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(3L, false);
        }

        @Test
        @DisplayName("handleDocumentEvent - si comando falla debe guardar error y hacer ACK")
        void handleDocumentEvent_commandThrows_shouldSaveProcessingError() throws Exception {
                DocumentEventDto event = validEvent();
                LogDocumentEventRequest request = mock(LogDocumentEventRequest.class);

                when(mapper.toRequest(event)).thenReturn(request);
                doThrow(new RuntimeException("DB error")).when(command).execute(request);

                listener.handleDocumentEvent(event, channel, 4L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                contains("[RuntimeException] DB error"),
                                anyString(),
                                eq("Document_event"),
                                eq("VALIDATION"));

                verify(channel).basicAck(4L, false);
        }

        @Test
        @DisplayName("handleDocumentEvent - debe validar campos obligatorios faltantes")
        void handleDocumentEvent_missingRequiredFields_shouldSaveValidationErrors() throws Exception {
                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId(" ")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "enterpriseId missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId(" ")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "documentId missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType(" ")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "documentType missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(null)
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "documentDate missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId(" ")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "userId missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName(" ")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "userName missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of())
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "userRoles missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType(null)
                                                .moduleName("DOCUMENTS")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "UNKNOWN",
                                "operationType missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName(" ")
                                                .operationAt(Instant.now())
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "moduleName missing");

                assertInvalidDocumentEvent(
                                DocumentEventDto.builder()
                                                .enterpriseId("ENT-001")
                                                .documentId("DOC-001")
                                                .documentCode("FAC-001")
                                                .documentType("FACTURA")
                                                .documentDate(LocalDate.now())
                                                .userId("USER-001")
                                                .userName("Freider")
                                                .userRoles(List.of("ADMIN"))
                                                .operationType("CREATE")
                                                .moduleName("DOCUMENTS")
                                                .operationAt(null)
                                                .documentData(Map.of("header", Map.of("documentCode", "FAC-001")))
                                                .build(),
                                "CREATE",
                                "operationAt missing");
        }

        @Test
        @DisplayName("handleDocumentEvent - documentData demasiado grande debe guardar error de validación")
        void handleDocumentEvent_documentDataTooLarge_shouldSaveValidationError() throws Exception {
                String largeText = "x".repeat(1_000_001);

                DocumentEventDto event = DocumentEventDto.builder()
                                .enterpriseId("ENT-001")
                                .documentId("DOC-001")
                                .documentCode("FAC-001")
                                .documentType("FACTURA")
                                .documentDate(LocalDate.now())
                                .userId("USER-001")
                                .userName("Freider")
                                .userRoles(List.of("ADMIN"))
                                .operationType("CREATE")
                                .moduleName("DOCUMENTS")
                                .operationAt(Instant.now())
                                .documentData(Map.of("payload", largeText))
                                .build();

                listener.handleDocumentEvent(event, channel, 77L);

                verify(errorPort).saveProcessingError(
                                eq("CREATE"),
                                eq("Validation failed: documentData exceeds max size"),
                                anyString(),
                                eq("Document_event"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(77L, false);
        }

        @Test
        @DisplayName("handleDocumentEvent - si falla serialización del evento debe guardar JSON de error")
        void handleDocumentEvent_jsonSerializationFails_shouldSaveFallbackJson() throws Exception {
                ObjectMapper failingMapper = mock(ObjectMapper.class);

                when(failingMapper.writeValueAsString(any()))
                                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {
                                });

                AuditDocumentEventDtoListener failingListener = new AuditDocumentEventDtoListener(command, mapper,
                                errorPort, failingMapper);

                failingListener.handleDocumentEvent(null, channel, 88L);

                verify(errorPort).saveProcessingError(
                                eq("UNKNOWN"),
                                eq("Validation failed: event is null"),
                                eq("{\"error\": \"Failed to convert\"}"),
                                eq("Document_event"),
                                eq("VALIDATION"));

                verify(channel).basicAck(88L, false);
        }

        private void assertInvalidDocumentEvent(DocumentEventDto event, String expectedEventType,
                        String expectedMessage)
                        throws Exception {

                listener.handleDocumentEvent(event, channel, 99L);

                verify(errorPort).saveProcessingError(
                                eq(expectedEventType),
                                eq("Validation failed: " + expectedMessage),
                                anyString(),
                                eq("Document_event"),
                                eq("VALIDATION"));

                verifyNoInteractions(mapper);
                verifyNoInteractions(command);
                verify(channel).basicAck(99L, false);

                clearInvocations(errorPort, mapper, command, channel);
        }
}
