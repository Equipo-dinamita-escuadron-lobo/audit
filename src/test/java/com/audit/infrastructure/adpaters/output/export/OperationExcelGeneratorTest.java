package com.audit.infrastructure.adpaters.output.export;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.OperationType;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.OperationData;
import com.audit.infrastructure.adapters.output.export.generator.OperationExcelGenerator;
import com.audit.infrastructure.adapters.output.export.helper.ExcelStyleHelper;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OperationExcelGeneratorTest {

    private final ExcelStyleHelper styleHelper = new ExcelStyleHelper();
    private final OperationExcelGenerator generator = new OperationExcelGenerator(styleHelper);

    private AuditOperation createOperation() {
        return AuditOperation.reconstruct(
                1L,
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                OperationType.CREATE,
                Instant.parse("2026-06-01T13:00:00Z"),
                "CONFIGURATION",
                "cost_centers",
                "1",
                "ENT-001",
                OperationData.forCreate(Map.of(
                        "id", 1L,
                        "name", "Centro principal",
                        "status", true)),
                Instant.now());
    }

    private AuditOperation updateOperation() {
        return AuditOperation.reconstruct(
                2L,
                "USER-002",
                "Admin",
                List.of("TEACHER"),
                OperationType.UPDATE,
                Instant.parse("2026-06-02T13:00:00Z"),
                "CONFIGURATION",
                "cost_centers",
                "2",
                "ENT-001",
                OperationData.forUpdate(
                        Map.of("ip", "127.0.0.1"),
                        Map.of("name", OperationData.FieldChange.of("Antiguo", "Nuevo"))),
                Instant.now());
    }

    @Test
    @DisplayName("generate - debe crear Excel válido de operaciones con snapshot CREATE")
    void generate_createOperation_shouldCreateExcel() throws Exception {
        byte[] bytes = generator.generate(
                List.of(createOperation()),
                "Empresa prueba",
                "Freider",
                "04/06/2026 10:00:00",
                "Usuario: Freider");

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Auditoría Operaciones");

            assertNotNull(sheet);
            assertEquals("Auditoría de Operaciones", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Empresa:", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Empresa prueba", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Generado por:", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Freider", sheet.getRow(2).getCell(1).getStringCellValue());
            assertEquals("Filtros aplicados:", sheet.getRow(4).getCell(0).getStringCellValue());

            int headerRow = 6;
            assertEquals("Usuario", sheet.getRow(headerRow).getCell(0).getStringCellValue());
            assertEquals("Rol", sheet.getRow(headerRow).getCell(1).getStringCellValue());
            assertEquals("Tipo Operación", sheet.getRow(headerRow).getCell(2).getStringCellValue());
            assertEquals("Detalles", sheet.getRow(headerRow).getCell(6).getStringCellValue());

            int dataRow = 7;
            assertEquals("Freider", sheet.getRow(dataRow).getCell(0).getStringCellValue());
            assertEquals("ADMIN", sheet.getRow(dataRow).getCell(1).getStringCellValue());
            assertFalse(sheet.getRow(dataRow).getCell(2).getStringCellValue().isBlank());
            assertFalse(sheet.getRow(dataRow).getCell(6).getStringCellValue().isBlank());
            assertTrue(sheet.getRow(dataRow).getCell(6).getStringCellValue().contains("Entidad:"));
        }
    }

    @Test
    @DisplayName("generate - debe crear Excel válido de operaciones con cambios UPDATE")
    void generate_updateOperation_shouldIncludeChanges() throws Exception {
        byte[] bytes = generator.generate(
                List.of(updateOperation()),
                "Empresa prueba",
                "Freider",
                "04/06/2026 10:00:00",
                "");

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Auditoría Operaciones");

            int headerRow = 5;
            int dataRow = 6;

            assertEquals("Usuario", sheet.getRow(headerRow).getCell(0).getStringCellValue());
            assertEquals("Admin", sheet.getRow(dataRow).getCell(0).getStringCellValue());
            assertEquals("TEACHER", sheet.getRow(dataRow).getCell(1).getStringCellValue());

            String details = sheet.getRow(dataRow).getCell(6).getStringCellValue();

            assertTrue(details.contains("Contexto:"));
            assertTrue(details.contains("Cambios:"));
            assertTrue(details.contains("Antes:"));
            assertTrue(details.contains("Después:"));
        }
    }

    @Test
    @DisplayName("generate - valores nulos deben escribirse como cadenas vacías")
    void generate_nullValues_shouldWriteEmptyStrings() throws Exception {
        AuditOperation operation = AuditOperation.reconstruct(
                1L,
                "USER-001",
                null,
                null,
                OperationType.CREATE,
                null,
                "CONFIGURATION",
                "cost_centers",
                "1",
                "ENT-001",
                OperationData.forCreate(Map.of("id", 1L)),
                Instant.now());

        byte[] bytes = generator.generate(
                List.of(operation),
                null,
                null,
                "04/06/2026 10:00:00",
                null);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Auditoría Operaciones");

            int dataRow = 6;

            assertEquals("", sheet.getRow(dataRow).getCell(0).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(1).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(3).getStringCellValue());
        }
    }
}
