package com.audit.infrastructure.adpaters.output.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.infrastructure.adapters.output.export.helper.DocumentAuditTranslationHelper;

class DocumentAuditTranslationHelperTest {

    @Test
    @DisplayName("translateField - debe traducir campos conocidos")
    void translateField_knownField_shouldReturnSpanish() {
        assertEquals("Código recibo", DocumentAuditTranslationHelper.translateField("receiptCode"));
        assertEquals("Nombre tercero", DocumentAuditTranslationHelper.translateField("thirdPartyName"));
        assertEquals("Código documento", DocumentAuditTranslationHelper.translateField("documentCode"));
        assertEquals("Estado", DocumentAuditTranslationHelper.translateField("status"));
    }

    @Test
    @DisplayName("translateField - campo desconocido debe retornar el mismo valor")
    void translateField_unknownField_shouldReturnOriginal() {
        assertEquals("UNKNOWN_FIELD", DocumentAuditTranslationHelper.translateField("UNKNOWN_FIELD"));
    }

    @Test
    @DisplayName("translateValue - valor nulo debe retornar em dash")
    void translateValue_null_shouldReturnDash() {
        assertEquals("—", DocumentAuditTranslationHelper.translateValue(null));
    }

    @Test
    @DisplayName("translateValue - booleano true debe retornar Sí")
    void translateValue_booleanTrue_shouldReturnSi() {
        assertEquals("Sí", DocumentAuditTranslationHelper.translateValue(true));
    }

    @Test
    @DisplayName("translateValue - booleano false debe retornar No")
    void translateValue_booleanFalse_shouldReturnNo() {
        assertEquals("No", DocumentAuditTranslationHelper.translateValue(false));
    }

    @Test
    @DisplayName("translateValue - valores conocidos deben traducirse")
    void translateValue_knownValue_shouldReturnSpanish() {
        assertEquals("Pago de factura", DocumentAuditTranslationHelper.translateValue("INVOICE_PAYMENT"));
        assertEquals("Confirmado", DocumentAuditTranslationHelper.translateValue("CONFIRMED"));
        assertEquals("Anulado", DocumentAuditTranslationHelper.translateValue("VOIDED"));
        assertEquals("Creación", DocumentAuditTranslationHelper.translateValue("CREATE"));
        assertEquals("Factura Venta", DocumentAuditTranslationHelper.translateValue("SALE"));
    }

    @Test
    @DisplayName("translateValue - lista de valores debe traducirse con join")
    void translateValue_listOfValues_shouldTranslateAndJoin() {
        List<String> values = List.of("CREATE", "UPDATE", "DELETE");
        String result = DocumentAuditTranslationHelper.translateValue(values);

        assertEquals("Creación, Actualización, Eliminación", result);
    }

    @Test
    @DisplayName("translateValue - valor desconocido debe retornar string original")
    void translateValue_unknownValue_shouldReturnOriginal() {
        assertEquals("UNKNOWN_VALUE", DocumentAuditTranslationHelper.translateValue("UNKNOWN_VALUE"));
    }

    @Test
    @DisplayName("isVisibleField - campos ocultos deben retornar false")
    void isVisibleField_hiddenFields_shouldReturnFalse() {
        assertFalse(DocumentAuditTranslationHelper.isVisibleField("id"));
        assertFalse(DocumentAuditTranslationHelper.isVisibleField("entId"));
        assertFalse(DocumentAuditTranslationHelper.isVisibleField("documentId"));
        assertFalse(DocumentAuditTranslationHelper.isVisibleField("parentId"));
    }

    @Test
    @DisplayName("isVisibleField - campos visibles deben retornar true")
    void isVisibleField_visibleFields_shouldReturnTrue() {
        assertTrue(DocumentAuditTranslationHelper.isVisibleField("code"));
        assertTrue(DocumentAuditTranslationHelper.isVisibleField("name"));
        assertTrue(DocumentAuditTranslationHelper.isVisibleField("description"));
        assertTrue(DocumentAuditTranslationHelper.isVisibleField("totalAmount"));
    }
}
