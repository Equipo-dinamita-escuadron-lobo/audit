package com.audit.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.exceptions.InvalidSnapshotDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentDataTest {

    @Test
    @DisplayName("of - datos válidos debe crear snapshot de documento")
    void of_validDocumentData() {
        DocumentData data = DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                List.of(Map.of("account", "1105", "debit", 1000)),
                Map.of("total", 1000),
                Map.of("source", "sales"));

        assertAll(
                () -> assertEquals("FAC-001", data.getHeader().get("documentCode")),
                () -> assertEquals(1, data.getDetails().size()),
                () -> assertEquals(1000, data.getTotals().get("total")),
                () -> assertEquals("sales", data.getMetadata().get("source")));
    }

    @Test
    @DisplayName("of - snapshot completamente vacío debe fallar")
    void of_completelyEmpty_throwsException() {
        assertThrows(InvalidSnapshotDataException.class, () -> DocumentData.of(null, null, null, null));
    }

    @Test
    @DisplayName("of - detalle nulo debe fallar")
    void of_nullDetailRow_throwsException() {
        List<Map<String, Object>> details = new ArrayList<>();
        details.add(null);

        assertThrows(InvalidSnapshotDataException.class, () -> DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                details,
                null,
                null));
    }

    @Test
    @DisplayName("of - detalle vacío debe fallar")
    void of_emptyDetailRow_throwsException() {
        assertThrows(InvalidSnapshotDataException.class, () -> DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                List.of(Map.of()),
                null,
                null));
    }

    @Test
    @DisplayName("of - llave vacía debe fallar")
    void of_blankKey_throwsException() {
        assertThrows(InvalidSnapshotDataException.class, () -> DocumentData.of(
                Map.of(" ", "valor"),
                null,
                null,
                null));
    }

    @Test
    @DisplayName("of - debe remover valores nulos al sanitizar")
    void of_sanitizeNullValues() {
        Map<String, Object> header = new java.util.HashMap<>();
        header.put("documentCode", "FAC-001");
        header.put("nullable", null);

        DocumentData data = DocumentData.of(header, null, null, null);

        assertAll(
                () -> assertEquals("FAC-001", data.getHeader().get("documentCode")),
                () -> assertFalse(data.getHeader().containsKey("nullable")));
    }
}
