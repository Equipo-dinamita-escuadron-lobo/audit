package com.audit.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.OperationType;
import com.audit.domain.exceptions.InvalidAuditEventException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditOperationTest {

        private static final String USER_ID = "USER-001";
        private static final String USER_NAME = "Freider";
        private static final List<String> ROLES = List.of("ADMIN");
        private static final String MODULE = "CONFIGURATION";
        private static final String TABLE = "cost_centers";
        private static final String REGISTER_ID = "1";
        private static final String ENTERPRISE_ID = "ENT-001";

        @Test
        @DisplayName("create - operación CREATE válida debe crear auditoría")
        void create_validCreateOperation() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L, "name", "Centro"));

                AuditOperation operation = AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data);

                assertAll(
                                () -> assertNull(operation.getId()),
                                () -> assertEquals(USER_ID, operation.getUserId()),
                                () -> assertEquals(OperationType.CREATE, operation.getOperationType()),
                                () -> assertEquals(TABLE, operation.getAffectedTable()),
                                () -> assertEquals(REGISTER_ID, operation.getRegisterId()),
                                () -> assertEquals(ENTERPRISE_ID, operation.getEnterpriseId()),
                                () -> assertFalse(operation.getDataObject().getEntity().isEmpty()),
                                () -> assertNotNull(operation.getCreatedAt()));
        }

        @Test
        @DisplayName("create - UPDATE válido debe aceptar cambios")
        void create_validUpdateOperation() {
                OperationData data = OperationData.forUpdate(Map.of(
                                "name", OperationData.FieldChange.of("Antiguo", "Nuevo")));

                AuditOperation operation = AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.UPDATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data);

                assertAll(
                                () -> assertEquals(OperationType.UPDATE, operation.getOperationType()),
                                () -> assertTrue(operation.getDataObject().getEntity().isEmpty()),
                                () -> assertFalse(operation.getDataObject().getChanges().isEmpty()));
        }

        @Test
        @DisplayName("create - CREATE con cambios debe fallar")
        void create_createWithChanges_throwsException() {
                OperationData data = OperationData.forUpdate(Map.of(
                                "name", OperationData.FieldChange.of("Antiguo", "Nuevo")));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - UPDATE con snapshot de entidad debe fallar")
        void create_updateWithEntitySnapshot_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.UPDATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si enterpriseId es vacío")
        void create_emptyEnterpriseId_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                " ",
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si operationAt es futuro")
        void create_futureOperationAt_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now().plusSeconds(301),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("reconstruct - debe reconstruir operación existente")
        void reconstruct_existingOperation() {
                Instant now = Instant.now();
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                AuditOperation operation = AuditOperation.reconstruct(
                                10L,
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.DELETE,
                                now,
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data,
                                now);

                assertAll(
                                () -> assertEquals(10L, operation.getId()),
                                () -> assertEquals(OperationType.DELETE, operation.getOperationType()),
                                () -> assertEquals(now, operation.getCreatedAt()));
        }

        @Test
        @DisplayName("create - DELETE válido debe aceptar snapshot de entidad")
        void create_validDeleteOperation() {

                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                AuditOperation operation = AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.DELETE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data);

                assertAll(
                                () -> assertEquals(OperationType.DELETE, operation.getOperationType()),
                                () -> assertFalse(operation.getDataObject().getEntity().isEmpty()),
                                () -> assertTrue(operation.getDataObject().getChanges().isEmpty()));
        }

        @Test
        @DisplayName("create - UPDATE sin changes debe fallar")
        void create_updateWithoutChanges_throwsException() {

                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.UPDATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - ACTIVATE válido debe requerir changes")
        void create_validActivateOperation() {

                OperationData data = OperationData.forUpdate(Map.of(
                                "active", OperationData.FieldChange.of(false, true)));

                AuditOperation operation = AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.ACTIVATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data);

                assertAll(
                                () -> assertEquals(OperationType.ACTIVATE, operation.getOperationType()),
                                () -> assertFalse(operation.getDataObject().getChanges().isEmpty()),
                                () -> assertTrue(operation.getDataObject().getEntity().isEmpty()));
        }

        @Test
        @DisplayName("create - INACTIVATE válido debe requerir changes")
        void create_validInactivateOperation() {

                OperationData data = OperationData.forUpdate(Map.of(
                                "active", OperationData.FieldChange.of(true, false)));

                AuditOperation operation = AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.INACTIVATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data);

                assertAll(
                                () -> assertEquals(OperationType.INACTIVATE, operation.getOperationType()),
                                () -> assertFalse(operation.getDataObject().getChanges().isEmpty()));
        }

        @Test
        @DisplayName("reconstruct - ACTIVATE debe reconstruir correctamente")
        void reconstruct_activateOperation() {

                Instant now = Instant.now();
                OperationData data = OperationData.forUpdate(Map.of(
                                "active", OperationData.FieldChange.of(false, true)));

                AuditOperation operation = AuditOperation.reconstruct(
                                1L,
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.ACTIVATE,
                                now,
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data,
                                now);

                assertEquals(OperationType.ACTIVATE, operation.getOperationType());
        }

        @Test
        @DisplayName("create - debe fallar si userId es null")
        void create_nullUserId_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                null,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si userId es vacío")
        void create_blankUserId_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                "   ",
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si userName es null")
        void create_nullUserName_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                null,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si userName es vacío")
        void create_blankUserName_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                "   ",
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si userRole es null")
        void create_nullUserRole_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                null,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si userRole es vacío")
        void create_emptyUserRole_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                List.of(),
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si operationType es null")
        void create_nullOperationType_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                null,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si affectedTable es null")
        void create_nullAffectedTable_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                null,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si affectedTable es vacío")
        void create_blankAffectedTable_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                "   ",
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si registerId es null")
        void create_nullRegisterId_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                null,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si registerId es vacío")
        void create_blankRegisterId_throwsException() {
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                "   ",
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - debe fallar si dataObject es null")
        void create_nullDataObject_throwsException() {
                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                null));
        }

        @Test
        @DisplayName("create - CREATE con changes no vacío debe fallar (caso que ya tienes pero por si acaso)")
        void create_createWithNonEmptyChanges_throwsException() {
                OperationData data = OperationData.forUpdate(Map.of(
                                "name", OperationData.FieldChange.of("Antiguo", "Nuevo")));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.CREATE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }

        @Test
        @DisplayName("create - DELETE con changes no vacío debe fallar")
        void create_deleteWithChanges_throwsException() {
                OperationData data = OperationData.forUpdate(Map.of(
                                "name", OperationData.FieldChange.of("Antiguo", "Nuevo")));

                assertThrows(InvalidAuditEventException.class, () -> AuditOperation.create(
                                USER_ID,
                                USER_NAME,
                                ROLES,
                                OperationType.DELETE,
                                Instant.now(),
                                MODULE,
                                TABLE,
                                REGISTER_ID,
                                ENTERPRISE_ID,
                                data));
        }
}
