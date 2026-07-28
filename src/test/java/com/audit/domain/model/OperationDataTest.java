package com.audit.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.exceptions.InvalidSnapshotDataException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OperationDataTest {

    @Test
    @DisplayName("forCreate - entidad válida debe crear data de operación")
    void forCreate_validEntity() {
        OperationData data = OperationData.forCreate(Map.of(
                "id", 1L,
                "name", "Centro de costos"));

        assertAll(
                () -> assertFalse(data.getEntity().isEmpty()),
                () -> assertTrue(data.getChanges().isEmpty()),
                () -> assertTrue(data.getContext().isEmpty()),
                () -> assertEquals("Centro de costos", data.getEntity().get("name")));
    }

    @Test
    @DisplayName("forCreate - entidad vacía debe fallar")
    void forCreate_emptyEntity_throwsException() {
        assertThrows(InvalidSnapshotDataException.class, () -> OperationData.forCreate(Map.of()));
    }

    @Test
    @DisplayName("forUpdate - cambios válidos debe crear data de cambios")
    void forUpdate_validChanges() {
        OperationData.FieldChange change = OperationData.FieldChange.of("old", "new");

        OperationData data = OperationData.forUpdate(Map.of("name", change));

        assertAll(
                () -> assertTrue(data.getEntity().isEmpty()),
                () -> assertFalse(data.getChanges().isEmpty()),
                () -> assertEquals("old", data.getChanges().get("name").getBefore()),
                () -> assertEquals("new", data.getChanges().get("name").getAfter()));
    }

    @Test
    @DisplayName("forUpdate - cambios vacíos debe fallar")
    void forUpdate_emptyChanges_throwsException() {
        assertThrows(InvalidSnapshotDataException.class, () -> OperationData.forUpdate(Map.of()));
    }

    @Test
    @DisplayName("forUpdate - nombre de campo vacío debe fallar")
    void forUpdate_blankChangeKey_throwsException() {
        assertThrows(InvalidSnapshotDataException.class, () -> OperationData.forUpdate(Map.of(
                " ", OperationData.FieldChange.of("old", "new"))));
    }

    @Test
    @DisplayName("forUpdate con contexto - debe conservar contexto y cambios")
    void forUpdate_withContext_valid() {
        OperationData data = OperationData.forUpdate(
                Map.of("ip", "127.0.0.1"),
                Map.of("status", OperationData.FieldChange.of(false, true)));

        assertAll(
                () -> assertEquals("127.0.0.1", data.getContext().get("ip")),
                () -> assertEquals(false, data.getChanges().get("status").getBefore()),
                () -> assertEquals(true, data.getChanges().get("status").getAfter()));
    }

    @Test
    @DisplayName("forCreate - entidad nula debe fallar")
    void forCreate_nullEntity_throwsException() {
        assertThrows(InvalidSnapshotDataException.class,
                () -> OperationData.forCreate(null));
    }

    @Test
    @DisplayName("forDelete - entidad nula debe fallar")
    void forDelete_nullEntity_throwsException() {
        assertThrows(InvalidSnapshotDataException.class,
                () -> OperationData.forDelete(null));
    }

    @Test
    @DisplayName("forDelete - entidad vacía debe fallar")
    void forDelete_emptyEntity_throwsException() {
        assertThrows(InvalidSnapshotDataException.class,
                () -> OperationData.forDelete(Map.of()));
    }

    @Test
    @DisplayName("forUpdate - cambios nulos debe fallar")
    void forUpdate_nullChanges_throwsException() {
        assertThrows(InvalidSnapshotDataException.class,
                () -> OperationData.forUpdate((Map<String, OperationData.FieldChange>) null));
    }

    @Test
    @DisplayName("forUpdate con contexto - cambios nulos debe fallar")
    void forUpdate_withContext_nullChanges_throwsException() {
        assertThrows(InvalidSnapshotDataException.class,
                () -> OperationData.forUpdate(
                        Map.of("ip", "127.0.0.1"),
                        null));
    }

    @Test
    @DisplayName("forUpdate con contexto - cambios vacíos debe fallar")
    void forUpdate_withContext_emptyChanges_throwsException() {
        assertThrows(InvalidSnapshotDataException.class,
                () -> OperationData.forUpdate(
                        Map.of("ip", "127.0.0.1"),
                        Map.of()));
    }

    @Test
    @DisplayName("forUpdate - nombre de campo nulo debe fallar")
    void forUpdate_nullChangeKey_throwsException() {

        Map<String, OperationData.FieldChange> changes = new HashMap<>();
        changes.put(null, OperationData.FieldChange.of("old", "new"));

        assertThrows(InvalidSnapshotDataException.class,
                () -> OperationData.forUpdate(changes));
    }

    @Test
    @DisplayName("forDelete - entidad válida debe crear snapshot de eliminación")
    void forDelete_validEntity() {
        OperationData data = OperationData.forDelete(Map.of("id", 1L));

        assertAll(
                () -> assertFalse(data.getEntity().isEmpty()),
                () -> assertTrue(data.getChanges().isEmpty()));
    }
}
