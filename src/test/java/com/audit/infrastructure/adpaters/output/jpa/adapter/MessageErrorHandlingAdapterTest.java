package com.audit.infrastructure.adpaters.output.jpa.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.audit.infrastructure.adapters.output.jpa.repository.IMessageErrorRepository;

import com.audit.infrastructure.adapters.output.jpa.adapter.MessageErrorHandlingAdapter;
import com.audit.infrastructure.adapters.output.jpa.entity.MessageErrorEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageErrorHandlingAdapterTest {

    @Test
    @DisplayName("saveProcessingError - debe construir y guardar error de procesamiento")
    void saveProcessingError_validData_shouldSaveEntity() {
        IMessageErrorRepository repository = mock(IMessageErrorRepository.class);
        MessageErrorHandlingAdapter adapter = new MessageErrorHandlingAdapter(repository);

        adapter.saveProcessingError(
                "SESSION_EVENT",
                "Error parseando mensaje",
                "{json}",
                "AuditSession",
                "CONSUMER");

        ArgumentCaptor<MessageErrorEntity> captor = ArgumentCaptor.forClass(MessageErrorEntity.class);
        verify(repository).save(captor.capture());

        MessageErrorEntity saved = captor.getValue();

        assertAll(
                () -> assertEquals("SESSION_EVENT", saved.getEventType()),
                () -> assertEquals("Error parseando mensaje", saved.getErrorDescription()),
                () -> assertEquals("{json}", saved.getMessageData()),
                () -> assertEquals("AuditSession", saved.getEntityType()),
                () -> assertEquals("CONSUMER", saved.getErrorStage()),
                () -> assertNotNull(saved.getErrorAt()));
    }

    @Test
    @DisplayName("saveProcessingError - si repository falla no debe propagar excepción")
    void saveProcessingError_repositoryThrows_shouldNotPropagateException() {
        IMessageErrorRepository repository = mock(IMessageErrorRepository.class);
        MessageErrorHandlingAdapter adapter = new MessageErrorHandlingAdapter(repository);

        when(repository.save(any(MessageErrorEntity.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> adapter.saveProcessingError(
                "OPERATION_EVENT",
                "Error",
                "{json}",
                "AuditOperation",
                "MAPPER"));
    }
}
