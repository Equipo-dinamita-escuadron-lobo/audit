package com.audit.infrastructure.adpaters.output.export;

import com.audit.application.internal.CombinedSession;
import com.audit.infrastructure.adapters.output.export.generator.SessionPdfGenerator;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionPdfGeneratorTest {

    private final SessionPdfGenerator generator = new SessionPdfGenerator();

    @Test
    @DisplayName("generate - debe generar PDF válido con datos de sesiones")
    void generate_validSessions_shouldCreatePdfFile() throws Exception {
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

        PdfReader reader = new PdfReader(bytes);
        String text = PdfTextExtractor.getTextFromPage(reader, 1);
        reader.close();

        assertAll(
                () -> assertTrue(text.contains("Auditoría de Sesiones")),
                () -> assertTrue(text.contains("Generado por: Freider")),
                () -> assertTrue(text.contains("Fecha de generación: 04/06/2026 10:00:00")),
                () -> assertTrue(text.contains("Filtros aplicados: Usuario: Freider")),
                () -> assertTrue(text.contains("Freider")),
                () -> assertTrue(text.contains("ADMIN, TEACHER")),
                () -> assertTrue(text.contains("Inicio de sesión")),
                () -> assertTrue(text.contains("Cierre de sesión")));
    }

    @Test
    @DisplayName("generate - sin filtros debe generar PDF sin línea de filtros")
    void generate_withoutFilters_shouldOmitFiltersText() throws Exception {
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

        PdfReader reader = new PdfReader(bytes);
        String text = PdfTextExtractor.getTextFromPage(reader, 1);
        reader.close();

        assertAll(
                () -> assertTrue(text.contains("Auditoría de Sesiones")),
                () -> assertFalse(text.contains("Filtros aplicados:")),
                () -> assertTrue(text.contains("Freider")),
                () -> assertTrue(text.contains("ADMIN")));
    }

    @Test
    @DisplayName("generate - valores nulos deben generar celdas vacías sin fallar")
    void generate_nullValues_shouldNotFail() {
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

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }
}
