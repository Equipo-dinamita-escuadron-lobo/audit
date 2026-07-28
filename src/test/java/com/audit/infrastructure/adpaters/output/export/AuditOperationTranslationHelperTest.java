package com.audit.infrastructure.adpaters.output.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.infrastructure.adapters.output.export.helper.AuditOperationTranslationHelper;

class AuditOperationTranslationHelperTest {

    @Test
    @DisplayName("translateModule - debe traducir módulos conocidos")
    void translateModule_knownModule_shouldReturnSpanish() {
        assertEquals("Contabilidad", AuditOperationTranslationHelper.translateModule("ACCOUNTING"));
        assertEquals("Configuración", AuditOperationTranslationHelper.translateModule("CONFIGURATION"));
        assertEquals("Inventario", AuditOperationTranslationHelper.translateModule("INVENTORY"));
    }

    @Test
    @DisplayName("translateModule - módulo desconocido debe retornar el mismo valor")
    void translateModule_unknownModule_shouldReturnOriginal() {
        assertEquals("UNKNOWN_MODULE", AuditOperationTranslationHelper.translateModule("UNKNOWN_MODULE"));
    }

    @Test
    @DisplayName("translateTable - debe traducir tablas conocidas")
    void translateTable_knownTable_shouldReturnSpanish() {
        assertEquals("Catálogo de cuentas", AuditOperationTranslationHelper.translateTable("ACCOUNT_CATALOGUE"));
        assertEquals("Usuario", AuditOperationTranslationHelper.translateTable("USER"));
        assertEquals("Perfil", AuditOperationTranslationHelper.translateTable("PROFILE"));
    }

    @Test
    @DisplayName("translateOperation - debe traducir operaciones")
    void translateOperation_knownOperation_shouldReturnSpanish() {
        assertEquals("Creación", AuditOperationTranslationHelper.translateOperation("CREATE"));
        assertEquals("Actualización", AuditOperationTranslationHelper.translateOperation("UPDATE"));
        assertEquals("Eliminación", AuditOperationTranslationHelper.translateOperation("DELETE"));
        assertEquals("Activación", AuditOperationTranslationHelper.translateOperation("ACTIVATE"));
        assertEquals("Inactivación", AuditOperationTranslationHelper.translateOperation("INACTIVATE"));
    }

    @Test
    @DisplayName("translateRole - debe traducir roles")
    void translateRole_knownRole_shouldReturnSpanish() {
        assertEquals("Administrador", AuditOperationTranslationHelper.translateRole("ADMINISTRADOR"));
        assertEquals("Profesor", AuditOperationTranslationHelper.translateRole("PROFESOR"));
        assertEquals("Estudiante", AuditOperationTranslationHelper.translateRole("ESTUDIANTE"));
    }

    @Test
    @DisplayName("isVisible - campos ocultos deben retornar false")
    void isVisible_hiddenFields_shouldReturnFalse() {
        assertFalse(AuditOperationTranslationHelper.isVisible("id"));
        assertFalse(AuditOperationTranslationHelper.isVisible("entId"));
        assertFalse(AuditOperationTranslationHelper.isVisible("parentId"));
        assertFalse(AuditOperationTranslationHelper.isVisible("children"));
    }

    @Test
    @DisplayName("isVisible - campos normales deben retornar true")
    void isVisible_visibleFields_shouldReturnTrue() {
        assertTrue(AuditOperationTranslationHelper.isVisible("code"));
        assertTrue(AuditOperationTranslationHelper.isVisible("name"));
        assertTrue(AuditOperationTranslationHelper.isVisible("description"));
    }

    @Test
    @DisplayName("translateFieldKey - debe traducir campos conocidos")
    void translateFieldKey_knownField_shouldReturnSpanish() {
        assertEquals("Código", AuditOperationTranslationHelper.translateFieldKey("code"));
        assertEquals("Nombre", AuditOperationTranslationHelper.translateFieldKey("name"));
        assertEquals("Estado", AuditOperationTranslationHelper.translateFieldKey("state"));
    }

    @Test
    @DisplayName("translateValue - debe traducir booleanos")
    void translateValue_boolean_shouldReturnActivoInactivo() {
        assertEquals("Activo", AuditOperationTranslationHelper.translateValue(true));
        assertEquals("Inactivo", AuditOperationTranslationHelper.translateValue(false));
    }

    @Test
    @DisplayName("translateValue - debe traducir valores conocidos")
    void translateValue_knownValue_shouldReturnSpanish() {
        assertEquals("Débito", AuditOperationTranslationHelper.translateValue("DEBIT"));
        assertEquals("Crédito", AuditOperationTranslationHelper.translateValue("CREDIT"));
        assertEquals("Persona natural", AuditOperationTranslationHelper.translateValue("NATURAL_PERSON"));
    }

    @Test
    @DisplayName("translateValue - valor nulo debe retornar em dash")
    void translateValue_null_shouldReturnDash() {
        assertEquals("—", AuditOperationTranslationHelper.translateValue(null));
    }

    @Test
    @DisplayName("translateValue - valor desconocido debe retornar string original")
    void translateValue_unknownValue_shouldReturnOriginal() {
        assertEquals("VALOR_DESCONOCIDO", AuditOperationTranslationHelper.translateValue("VALOR_DESCONOCIDO"));
    }
}
