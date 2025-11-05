package com.audit.infrastructure.adapters.output.export.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.audit.application.dto.responses.SessionAuditResponse;
import com.audit.application.port.output.FileExportService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
//import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @brief Service implementation that exports audit sessions to Excel or PDF
 *        formats.
 * 
 *        Uses Apache POI for Excel and iText for PDF generation.
 */
@Service
@Slf4j
public class FileExportServiceImpl implements FileExportService {

    //private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] exportSessions(List<SessionAuditResponse> sessions, String format) {
        log.info("Exporting {} sessions to {} format", sessions.size(), format);

        try {
            if ("PDF".equalsIgnoreCase(format)) {
                return exportToPdf(sessions);
            } else {
                return exportToExcel(sessions);
            }
        } catch (Exception e) {
            log.error("Error exporting sessions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export audit sessions", e);
        }
    }

    /**
     * Generates an Excel file using Apache POI.
     */
    private byte[] exportToExcel(List<SessionAuditResponse> sessions) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Audit Sessions");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "User ID", "User Name", "Role", "Action", "Action Time", "IP Address",
                    "Enterprise ID" };

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (SessionAuditResponse session : sessions) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(2).setCellValue(session.getUserName());
                row.createCell(3).setCellValue(session.getUserRole());

                org.apache.poi.ss.usermodel.Cell dateCell = row.createCell(5);
                dateCell.setCellStyle(dateStyle);
            }

            // Adjust column widths
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Excel file generated successfully with {} records", sessions.size());
            return out.toByteArray();
        }
    }

    /**
     * Generates a PDF file using iText.
     */
    private byte[] exportToPdf(List<SessionAuditResponse> sessions) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            Paragraph title = new Paragraph("Audit Sessions Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Table
            float[] columnWidths = { 1, 2, 3, 2, 2, 3, 2, 2 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = { "ID", "User ID", "User Name", "Role", "Action", "Action Time", "IP Address",
                    "Enterprise" };
            for (String header : headers) {
                table.addHeaderCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(header).setBold())
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            }

            for (SessionAuditResponse session : sessions) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(session.getUserName())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(session.getUserRole())));
            }
            document.add(table);

            // Footer
            Paragraph footer = new Paragraph(String.format("\nTotal Records: %d", sessions.size()))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(footer);

            document.close();
            log.info("PDF file generated successfully with {} records", sessions.size());
            return out.toByteArray();
        }
    }

    /** Helper to style header cells */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /** Helper to style date cells */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }
}
