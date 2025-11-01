package com.audit.infrastructure.adapters.input.messageBroker.base;

import org.springframework.amqp.core.Message;
import com.audit.domain.port.messageProcessingError.IMessageErrorHandlingPort;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase base abstracta para todos los message listeners de RabbitMQ.
 * Proporciona funcionalidad común para el manejo de mensajes sin lógica de DLQ.
 * 
 * @param <T> Tipo del evento/mensaje a procesar
 */
@Slf4j
public abstract class AbstractMessageListener<T> {

    /**
     * Puerto para el manejo de errores de procesamiento.
     * Debe ser inyectado por las clases hijas.
     */
    protected IMessageErrorHandlingPort messageErrorHandlingPort;

    /**
     * Método principal para manejar mensajes entrantes.
     * Implementa la lógica común de validación, procesamiento y acknowledgment.
     */
    protected void handleMessage(T event, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());

            if (!isValidEvent(event)) {
                log.warn("Invalid {} event received, saving error to database", getEntityType());
                handleValidationError(event);
                acknowledgeMessage(channel, deliveryTag);
                return;
            }

            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());

        } catch (Exception e) {
            handleProcessingError(e, event, channel, deliveryTag);
        }
    }

    /**
     * Procesa el evento específico. Debe ser implementado por cada listener.
     */
    protected abstract void processEvent(T event);

    /**
     * Valida si el evento es válido para procesamiento.
     */
    protected abstract boolean isValidEvent(T event);

    /**
     * Retorna el tipo de entidad que maneja este listener (para logging).
     */
    protected abstract String getEntityType();

    /**
     * Maneja errores durante el procesamiento del mensaje.
     */
    private void handleProcessingError(Exception e, T event, Channel channel, long deliveryTag) {
        try {
            log.error("Error processing {} message: {}", getEntityType(), e.getMessage(), e);

            // Guardar error en base de datos
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = String.format("Processing error: %s",
                        e.getMessage());

                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }

            acknowledgeMessage(channel, deliveryTag); // ACK para evitar reenvío
        } catch (Exception ackException) {
            log.error("Error acknowledging message: {}", ackException.getMessage());
        }
    }

    /**
     * Maneja errores de validación de eventos.
     */
    private void handleValidationError(T event) {
        try {
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String specificError = getValidationErrorMessage();
                String errorDescription = specificError != null
                        ? "Validation failed: " + specificError
                        : "Validation failed: Required fields are missing or invalid";

                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }
        } catch (Exception e) {
            log.error("Error saving validation error to database: {}", e.getMessage());
        }
    }

    /**
     * Envía acknowledgment del mensaje.
     */
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: {}", e.getMessage());
        }
    }

    /**
     * Método de utilidad para extraer contenido del mensaje como String.
     */
    protected String getMessageBodyAsString(Message message) {
        try {
            return new String(message.getBody());
        } catch (Exception e) {
            log.warn("Error converting message body to string: {}", e.getMessage());
            return "unavailable";
        }
    }

    /**
     * Extrae el tipo de evento del mensaje. Debe ser implementado por cada
     * listener.
     * 
     * @param event El evento del cual extraer el tipo
     * @return String representando el tipo de evento, o null si no se puede
     *         determinar
     */
    protected abstract String extractEventType(T event);

    /**
     * Convierte el evento a JSON para almacenamiento en BD. Debe ser implementado
     * por cada listener.
     * 
     * @param event El evento a convertir
     * @return String en formato JSON con los datos del evento
     */
    protected abstract String convertEventToJson(T event);

    /**
     * Obtiene el mensaje de error específico de validación si está disponible.
     * 
     * @return String con el mensaje de error específico, o null si no hay mensaje
     *         específico
     */
    protected String getValidationErrorMessage() {
        return null; // Implementación por defecto
    }
}
