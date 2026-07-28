package com.audit.infrastructure.adpaters.output.export;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.internal.CombinedSession;
import com.audit.infrastructure.adapters.output.export.generator.SessionExcelGenerator;
import com.audit.infrastructure.adapters.output.export.helper.ExcelStyleHelper;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionExcelGeneratorTest {

    private final ExcelStyleHelper styleHelper = new ExcelStyleHelper();
    private final SessionExcelGenerator generator = new SessionExcelGenerator(styleHelper);

    @Test
    @DisplayName("generate - debe generar archivo Excel válido con datos de sesiones")
    void generate_validSessions_shouldCreateExcelFile() throws Exception {
        CombinedSession session = CombinedSession.of(
                "SESSION-001",
                "Freider",
                List.of("ADMIN", "TEACHER"),
                Instant.parse("2026-06-01T13:00:00Z"),
                Instant.parse("2026-06-01T15:00:00Z"));

        byte[] bytes = generator.generate(
                List.of(session),
                "Freider",
                "04/06/2026 10:00:00",
                "Usuario: Freider");

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Sesiones");

            assertNotNull(sheet);
            assertEquals("Auditoría de Sesiones", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Generado por:", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Freider", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Fecha de generación:", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Filtros aplicados:", sheet.getRow(3).getCell(0).getStringCellValue());

            int headerRowIndex = 5;
            assertEquals("Usuario", sheet.getRow(headerRowIndex).getCell(0).getStringCellValue());
            assertEquals("Rol", sheet.getRow(headerRowIndex).getCell(1).getStringCellValue());
            assertEquals("Inicio de sesión", sheet.getRow(headerRowIndex).getCell(2).getStringCellValue());
            assertEquals("Cierre de sesión", sheet.getRow(headerRowIndex).getCell(3).getStringCellValue());

            int dataRowIndex = 6;
            assertEquals("Freider", sheet.getRow(dataRowIndex).getCell(0).getStringCellValue());
            assertEquals("ADMIN, TEACHER", sheet.getRow(dataRowIndex).getCell(1).getStringCellValue());
            assertFalse(sheet.getRow(dataRowIndex).getCell(2).getStringCellValue().isBlank());
            assertFalse(sheet.getRow(dataRowIndex).getCell(3).getStringCellValue().isBlank());
        }
    }

    @Test
    @DisplayName("generate - sin filtros debe omitir fila de filtros")
    void generate_withoutFilters_shouldOmitFilterRow() throws Exception {
        CombinedSession session = CombinedSession.of(
                "SESSION-001",
                "Freider",
                List.of("ADMIN"),
                Instant.parse("2026-06-01T13:00:00Z"),
                null);

        byte[] bytes = generator.generate(
                List.of(session),
                "Freider",
                "04/06/2026 10:00:00",
                "");

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Sesiones");

            int headerRowIndex = 4;

            assertEquals("Usuario", sheet.getRow(headerRowIndex).getCell(0).getStringCellValue());
            assertEquals("Freider", sheet.getRow(headerRowIndex + 1).getCell(0).getStringCellValue());
            assertEquals("ADMIN", sheet.getRow(headerRowIndex + 1).getCell(1).getStringCellValue());
            assertEquals("", sheet.getRow(headerRowIndex + 1).getCell(3).getStringCellValue());
        }
    }

    @Test
    @DisplayName("generate - valores nulos deben escribirse como cadenas vacías")
    void generate_nullValues_shouldWriteEmptyStrings() throws Exception {
        CombinedSession session = CombinedSession.of(
                "SESSION-001",
                null,
                null,
                Instant.parse("2026-06-01T13:00:00Z"),
                null);

        byte[] bytes = generator.generate(
                List.of(session),
                "Freider",
                "04/06/2026 10:00:00",
                null);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Sesiones");

            int dataRowIndex = 5;

            assertEquals("", sheet.getRow(dataRowIndex).getCell(0).getStringCellValue());
            assertEquals("", sheet.getRow(dataRowIndex).getCell(1).getStringCellValue());
            assertEquals("", sheet.getRow(dataRowIndex).getCell(3).getStringCellValue());
        }
    }
}
