package com.audit.infrastructure.adpaters.output.export;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.model.AuditDocumentEvent;
import com.audit.domain.model.DocumentData;
import com.audit.infrastructure.adapters.output.export.generator.DocumentExcelGenerator;
import com.audit.infrastructure.adapters.output.export.helper.ExcelStyleHelper;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentExcelGeneratorTest {

    private final ExcelStyleHelper styleHelper = new ExcelStyleHelper();
    private final DocumentExcelGenerator generator = new DocumentExcelGenerator(styleHelper);

    private AuditDocumentEvent eventWithSnapshot() {
        DocumentData data = DocumentData.of(
                Map.of(
                        "documentCode", "FAC-001",
                        "thirdPartyName", "Cliente prueba",
                        "changes", Map.of(
                                "thirdPartyName", Map.of("before", "Cliente A", "after", "Cliente B"))),
                List.of(Map.of(
                        "invoiceCode", "LIN-001",
                        "productId", "PROD-001",
                        "quantity", 2,
                        "changes", Map.of(
                                "quantity", Map.of("before", 1, "after", 2)))),
                Map.of(
                        "total", 1000,
                        "changes", Map.of(
                                "total", Map.of("before", 900, "after", 1000))),
                Map.of("source", "sales"));

        return AuditDocumentEvent.reconstruct(
                1L,
                "ENT-001",
                "FAC-001",
                "FACTURA",
                "DOC-001",
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                DocumentOperationType.UPDATE,
                "TP-001",
                "Cliente prueba",
                "DOCUMENTS",
                Instant.parse("2026-06-01T13:00:00Z"),
                LocalDate.of(2026, 6, 1),
                data,
                Instant.now());
    }

    @Test
    @DisplayName("generate - debe crear Excel válido de auditoría documental")
    void generate_validDocumentEvents_shouldCreateExcel() throws Exception {
        byte[] bytes = generator.generate(
                List.of(eventWithSnapshot()),
                "Empresa prueba",
                "Freider",
                "04/06/2026 10:00:00",
                "Documento: FAC-001");

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Auditoría Documentos");

            assertNotNull(sheet);
            assertEquals("Auditoría de Eventos de Documentos", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Empresa:", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Empresa prueba", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Generado por:", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Freider", sheet.getRow(2).getCell(1).getStringCellValue());
            assertEquals("Filtros aplicados:", sheet.getRow(4).getCell(0).getStringCellValue());

            int headerRow = 6;
            assertEquals("Código Documento", sheet.getRow(headerRow).getCell(0).getStringCellValue());
            assertEquals("Tipo Documento", sheet.getRow(headerRow).getCell(1).getStringCellValue());
            assertEquals("Encabezado", sheet.getRow(headerRow).getCell(9).getStringCellValue());
            assertEquals("Detalle", sheet.getRow(headerRow).getCell(10).getStringCellValue());
            assertEquals("Totales", sheet.getRow(headerRow).getCell(11).getStringCellValue());

            int dataRow = 7;
            assertEquals("FAC-001", sheet.getRow(dataRow).getCell(0).getStringCellValue());
            assertEquals("Cliente prueba", sheet.getRow(dataRow).getCell(2).getStringCellValue());
            assertEquals("2026-06-01", sheet.getRow(dataRow).getCell(3).getStringCellValue());
            assertEquals("Freider", sheet.getRow(dataRow).getCell(4).getStringCellValue());
            assertEquals("ADMIN", sheet.getRow(dataRow).getCell(5).getStringCellValue());

            assertFalse(sheet.getRow(dataRow).getCell(9).getStringCellValue().isBlank());
            assertFalse(sheet.getRow(dataRow).getCell(10).getStringCellValue().isBlank());
            assertFalse(sheet.getRow(dataRow).getCell(11).getStringCellValue().isBlank());

            assertTrue(sheet.getRow(dataRow).getCell(9).getStringCellValue().contains("Cambios:"));
            assertTrue(sheet.getRow(dataRow).getCell(10).getStringCellValue().contains("Cambios:"));
            assertTrue(sheet.getRow(dataRow).getCell(11).getStringCellValue().contains("Cambios:"));
        }
    }

    @Test
    @DisplayName("generate - sin filtros debe omitir fila de filtros")
    void generate_withoutFilters_shouldOmitFilterRow() throws Exception {
        byte[] bytes = generator.generate(
                List.of(eventWithSnapshot()),
                "Empresa prueba",
                "Freider",
                "04/06/2026 10:00:00",
                "");

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Auditoría Documentos");

            int headerRow = 5;
            int dataRow = 6;

            assertEquals("Código Documento", sheet.getRow(headerRow).getCell(0).getStringCellValue());
            assertEquals("FAC-001", sheet.getRow(dataRow).getCell(0).getStringCellValue());
        }
    }

    @Test
    @DisplayName("generate - si documentData es nulo debe dejar snapshots vacíos")
    void generate_nullDocumentData_shouldWriteEmptySnapshotColumns() throws Exception {
        AuditDocumentEvent event = AuditDocumentEvent.reconstruct(
                1L,
                "ENT-001",
                "FAC-001",
                "FACTURA",
                "DOC-001",
                "USER-001",
                "Freider",
                null,
                null,
                "TP-001",
                "Cliente prueba",
                null,
                null,
                null,
                null,
                Instant.now());

        byte[] bytes = generator.generate(
                List.of(event),
                null,
                null,
                "04/06/2026 10:00:00",
                null);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Auditoría Documentos");

            int dataRow = 6;

            assertEquals("FAC-001", sheet.getRow(dataRow).getCell(0).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(5).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(6).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(7).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(8).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(9).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(10).getStringCellValue());
            assertEquals("", sheet.getRow(dataRow).getCell(11).getStringCellValue());
        }
    }

    @Test
    @DisplayName("generate - detalle sin identificador debe usar Línea N")
    void generate_detailWithoutIdentifier_shouldUseLineLabel() throws Exception {
        DocumentData data = DocumentData.of(
                Map.of("documentCode", "FAC-001"),
                List.of(Map.of("description", "Detalle sin código")),
                Map.of("total", 1000),
                Map.of());

        AuditDocumentEvent event = AuditDocumentEvent.reconstruct(
                1L,
                "ENT-001",
                "FAC-001",
                "FACTURA",
                "DOC-001",
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                DocumentOperationType.CREATE,
                "TP-001",
                "Cliente prueba",
                "DOCUMENTS",
                Instant.now(),
                LocalDate.now(),
                data,
                Instant.now());

        byte[] bytes = generator.generate(
                List.of(event),
                "Empresa prueba",
                "Freider",
                "04/06/2026 10:00:00",
                "");

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Auditoría Documentos");

            String detailText = sheet.getRow(6).getCell(10).getStringCellValue();

            assertTrue(detailText.contains("Línea 1"));
        }
    }
}
