package com.audit.infrastructure.adapters.output.export.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.SessionAuditResponse;
import com.audit.application.port.output.FileExportService;
import com.audit.infrastructure.adapters.output.exception.security.ExportException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class FileExportServiceImpl implements FileExportService {

    private static final String[] SESSION_HEADERS_EXCEL = {
            "Usuario", "Rol", "Inicio de sesión", "Cierre de sesión"
    };

    // private static final String[] SESSION_HEADERS_PDF = {
    //         "N°", "Usuario", "Rol", "Inicio de sesión", "Cierre de sesión"
    // };
    private static final String[] OPERATION_HEADERS = {
            "Usuario", "Rol", "Tipo de operación", "Módulo",
            "Tabla afectada", "ID Registro", "Fecha operación"
    };

    @Override
    public byte[] exportSessions(List<SessionAuditResponse> sessions, String format) {
        if ("PDF".equalsIgnoreCase(format)) {
            return exportSessionsPdf(sessions);
        }
        return exportSessionsExcel(sessions);
    }

    @Override
    public byte[] exportOperations(List<OperationAuditResponse> operations, String format) {
        return exportOperationsExcel(operations);
    }

    private byte[] exportSessionsExcel(List<SessionAuditResponse> sessions) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Sesiones");
            writeHeaders(sheet, SESSION_HEADERS_EXCEL, workbook);

            AtomicInteger rowNum = new AtomicInteger(1);
            sessions.forEach(session -> {
                Row row = sheet.createRow(rowNum.getAndIncrement());
                row.createCell(0).setCellValue(session.getUserName());
                row.createCell(1).setCellValue(session.getUserRole());
                row.createCell(2).setCellValue(formatDateTime(session.getLoginTime()));
                row.createCell(3).setCellValue(
                        session.getLogoutTime() != null ? formatDateTime(session.getLogoutTime()) : "Sesión activa");
            });

            workbook.write(baos);
            workbook.dispose();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new ExportException("Error generating Excel for sessions", e);
        }
    }

    private byte[] exportOperationsExcel(List<OperationAuditResponse> operations) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Operaciones");
            writeHeaders(sheet, OPERATION_HEADERS, workbook);

            AtomicInteger rowNum = new AtomicInteger(1);
            operations.forEach(op -> {
                Row row = sheet.createRow(rowNum.getAndIncrement());
                row.createCell(0).setCellValue(op.getUserName());
                row.createCell(1).setCellValue(op.getUserRole());
                row.createCell(2).setCellValue(op.getOperationType());
                row.createCell(3).setCellValue(op.getModuleName());
                row.createCell(4).setCellValue(op.getAffectedTable());
                row.createCell(5).setCellValue(op.getRegisterId());
                row.createCell(6).setCellValue(formatDateTime(op.getOperationAt()));
            });

            workbook.write(baos);
            workbook.dispose();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new ExportException("Error generating Excel for operations", e);
        }
    }

    private byte[] exportSessionsPdf(List<SessionAuditResponse> sessions) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph(
                    "Reporte de Sesiones de Auditoría",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

            document.add(new Paragraph("Generado: " + formatDateTime(Instant.now())));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(SESSION_HEADERS_EXCEL.length);
            table.setWidthPercentage(100);

            // Headers
            for (String header : SESSION_HEADERS_EXCEL) {
                PdfPCell cell = new PdfPCell(new Phrase(
                        header,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data
            //AtomicInteger counter = new AtomicInteger(1);
            sessions.forEach(session -> {
                // table.addCell(String.valueOf(counter.getAndIncrement()));
                table.addCell(session.getUserName());
                table.addCell(session.getUserRole());
                table.addCell(formatDateTime(session.getLoginTime()));
                table.addCell(session.getLogoutTime() != null
                        ? formatDateTime(session.getLogoutTime())
                        : "");
            });

            document.add(table);
            document.close();

            return baos.toByteArray();

        } catch (DocumentException e) {
            throw new ExportException("Error generating PDF for sessions", e);
        }
    }

    private void writeHeaders(Sheet sheet, String[] headers, SXSSFWorkbook workbook) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private String formatDateTime(Instant instant) {
        if (instant == null) return "";
        return instant
                .atZone(ZoneId.of("America/Bogota"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

}
