package com.audit.infrastructure.adpaters.input.messageBroker;

import com.audit.application.port.output.IMessageErrorHandlingPort;
import com.audit.infrastructure.adapters.input.messageBroker.base.AbstractMessageListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AbstractMessageListenerTest {

    private static class TestListener extends AbstractMessageListener<String> {

        private boolean shouldFailProcessing = false;
        private Optional<String> validationResult = Optional.empty();

        TestListener(IMessageErrorHandlingPort errorPort, ObjectMapper objectMapper) {
            super(errorPort, objectMapper);
        }

        void exposeHandleMessage(String event, Channel channel, long deliveryTag) {
            handleMessage(event, channel, deliveryTag);
        }

        boolean exposeIsValidJsonSize(Object data, int maxBytes) {
            return isValidJsonSize(data, maxBytes);
        }

        String exposeGetMessageBodyAsString(Message message) {
            return getMessageBodyAsString(message);
        }

        void setValidationResult(Optional<String> validationResult) {
            this.validationResult = validationResult;
        }

        void setShouldFailProcessing(boolean shouldFailProcessing) {
            this.shouldFailProcessing = shouldFailProcessing;
        }

        @Override
        protected void processEvent(String event) {
            if (shouldFailProcessing) {
                throw new RuntimeException("Processing failed");
            }
        }

        @Override
        protected Optional<String> validateEvent(String event) {
            return validationResult;
        }

        @Override
        protected String getEntityType() {
            return "TestEntity";
        }

        @Override
        protected String extractEventType(String event) {
            return "TEST_EVENT";
        }

        @Override
        protected String convertEventToJson(String event) {
            return "{\"event\":\"" + event + "\"}";
        }
    }

    @Test
    @DisplayName("handleMessage - evento válido debe procesar y hacer ACK")
    void handleMessage_validEvent_shouldProcessAndAck() throws Exception {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        TestListener listener = new TestListener(errorPort, new ObjectMapper());

        listener.exposeHandleMessage("valid-event", channel, 10L);

        verify(channel).basicAck(10L, false);
        verifyNoInteractions(errorPort);
    }

    @Test
    @DisplayName("handleMessage - evento inválido debe guardar error y hacer ACK")
    void handleMessage_invalidEvent_shouldSaveErrorAndAck() throws Exception {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        TestListener listener = new TestListener(errorPort, new ObjectMapper());
        listener.setValidationResult(Optional.of("field missing"));

        listener.exposeHandleMessage("invalid-event", channel, 11L);

        verify(errorPort).saveProcessingError(
                eq("TEST_EVENT"),
                eq("Validation failed: field missing"),
                contains("invalid-event"),
                eq("TestEntity"),
                eq("VALIDATION"));

        verify(channel).basicAck(11L, false);
    }

    @Test
    @DisplayName("handleMessage - error procesando debe guardar error y hacer ACK")
    void handleMessage_processingError_shouldSaveErrorAndAck() throws Exception {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        TestListener listener = new TestListener(errorPort, new ObjectMapper());
        listener.setShouldFailProcessing(true);

        listener.exposeHandleMessage("event", channel, 12L);

        verify(errorPort).saveProcessingError(
                eq("TEST_EVENT"),
                contains("[RuntimeException] Processing failed"),
                contains("event"),
                eq("TestEntity"),
                eq("VALIDATION"));

        verify(channel).basicAck(12L, false);
    }

    @Test
    @DisplayName("handleMessage - si ACK falla no debe propagar excepción")
    void handleMessage_ackFails_shouldNotPropagateException() throws Exception {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        doThrow(new RuntimeException("ACK error"))
                .when(channel).basicAck(13L, false);

        TestListener listener = new TestListener(errorPort, new ObjectMapper());

        assertDoesNotThrow(() -> listener.exposeHandleMessage("event", channel, 13L));
    }

    @Test
    @DisplayName("isValidJsonSize - debe validar tamaño máximo de JSON")
    void isValidJsonSize_shouldValidateMaxBytes() {
        TestListener listener = new TestListener(null, new ObjectMapper());

        assertAll(
                () -> assertTrue(listener.exposeIsValidJsonSize("abc", 20)),
                () -> assertFalse(listener.exposeIsValidJsonSize("texto demasiado largo", 5)));
    }

    @Test
    @DisplayName("handleMessage - error con causa raíz debe incluir Caused by en descripción")
    void handleMessage_processingErrorWithRootCause_shouldIncludeCause() throws Exception {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        TestListener listener = new TestListener(errorPort, new ObjectMapper()) {
            @Override
            protected void processEvent(String event) {
                throw new RuntimeException("Service error",
                        new IllegalStateException("Root cause error"));
            }
        };

        listener.exposeHandleMessage("event", channel, 20L);

        verify(errorPort).saveProcessingError(
                eq("TEST_EVENT"),
                contains("Caused by: [IllegalStateException] Root cause error"),
                contains("event"),
                eq("TestEntity"),
                eq("VALIDATION"));

        verify(channel).basicAck(20L, false);
    }

    @Test
    @DisplayName("handleMessage - error sin mensaje debe registrar No message")
    void handleMessage_processingErrorWithoutMessage_shouldUseNoMessage() throws Exception {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        TestListener listener = new TestListener(errorPort, new ObjectMapper()) {
            @Override
            protected void processEvent(String event) {
                throw new RuntimeException();
            }
        };

        listener.exposeHandleMessage("event", channel, 21L);

        verify(errorPort).saveProcessingError(
                eq("TEST_EVENT"),
                contains("[RuntimeException] No message"),
                contains("event"),
                eq("TestEntity"),
                eq("VALIDATION"));

        verify(channel).basicAck(21L, false);
    }

    @Test
    @DisplayName("handleMessage - error procesando sin puerto de errores debe hacer ACK")
    void handleMessage_processingErrorWithoutErrorPort_shouldAckOnly() throws Exception {
        Channel channel = mock(Channel.class);

        TestListener listener = new TestListener(null, new ObjectMapper());
        listener.setShouldFailProcessing(true);

        listener.exposeHandleMessage("event", channel, 22L);

        verify(channel).basicAck(22L, false);
    }

    @Test
    @DisplayName("handleMessage - validación fallida sin puerto de errores debe hacer ACK")
    void handleMessage_validationErrorWithoutErrorPort_shouldAckOnly() throws Exception {
        Channel channel = mock(Channel.class);

        TestListener listener = new TestListener(null, new ObjectMapper());
        listener.setValidationResult(Optional.of("invalid data"));

        listener.exposeHandleMessage("event", channel, 23L);

        verify(channel).basicAck(23L, false);
    }

    @Test
    @DisplayName("handleMessage - si guardar error y ACK fallan no debe propagar excepción")
    void handleMessage_saveErrorAndAckFail_shouldNotPropagate() throws Exception {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        doThrow(new RuntimeException("error repository failed"))
                .when(errorPort)
                .saveProcessingError(any(), any(), any(), any(), any());

        doThrow(new RuntimeException("ack failed"))
                .when(channel)
                .basicAck(25L, false);

        TestListener listener = new TestListener(errorPort, new ObjectMapper());
        listener.setShouldFailProcessing(true);

        assertDoesNotThrow(() -> listener.exposeHandleMessage("event", channel, 25L));

        verifyNoInteractions(channel);
    }

    @Test
    @DisplayName("handleMessage - si guardar error falla no debe propagar excepción")
    void handleMessage_saveErrorFails_shouldNotPropagateException() {
        IMessageErrorHandlingPort errorPort = mock(IMessageErrorHandlingPort.class);
        Channel channel = mock(Channel.class);

        doThrow(new RuntimeException("error repository failed"))
                .when(errorPort)
                .saveProcessingError(any(), any(), any(), any(), any());

        TestListener listener = new TestListener(errorPort, new ObjectMapper());
        listener.setShouldFailProcessing(true);

        assertDoesNotThrow(() -> listener.exposeHandleMessage("event", channel, 24L));

        verifyNoInteractions(channel);
    }

    @Test
    @DisplayName("isValidJsonSize - si ObjectMapper falla debe retornar false")
    void isValidJsonSize_objectMapperFails_shouldReturnFalse() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);

        when(mapper.writeValueAsString(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {
                });

        TestListener listener = new TestListener(null, mapper);

        assertFalse(listener.exposeIsValidJsonSize(Map.of("key", "value"), 100));
    }

    @Test
    @DisplayName("getMessageBodyAsString - si body es nulo debe retornar unavailable")
    void getMessageBodyAsString_nullBody_shouldReturnUnavailable() {
        TestListener listener = new TestListener(null, new ObjectMapper());

        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(null);

        String result = listener.exposeGetMessageBodyAsString(message);

        assertEquals("unavailable", result);
    }

    @Test
    @DisplayName("getMessageBodyAsString - debe convertir body a String")
    void getMessageBodyAsString_shouldReturnBodyAsString() {
        TestListener listener = new TestListener(null, new ObjectMapper());

        Message message = new Message("hola".getBytes(StandardCharsets.UTF_8));

        assertEquals("hola", listener.exposeGetMessageBodyAsString(message));
    }

}