package com.audit.application.dto.response;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportFileResponse {

    private byte[] fileContent;
    private String fileName;
    private String contentType;
    private long fileSize;
    private int totalRecords;
    private String format;
    private ZonedDateTime generatedAt;

    public static ExportFileResponse excel(byte[] content, String fileName, int recordCount) {
        return ExportFileResponse.builder()
                .fileContent(content)
                .fileName(fileName)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .fileSize((long) content.length)
                .totalRecords(recordCount)
                .format("EXCEL")
                .generatedAt(ZonedDateTime.now())
                .build();
    }

    public static ExportFileResponse pdf(byte[] content, String fileName, int recordCount) {
        return ExportFileResponse.builder()
                .fileContent(content)
                .fileName(fileName)
                .contentType("application/pdf")
                .fileSize((long) content.length)
                .totalRecords(recordCount)
                .format("PDF")
                .generatedAt(ZonedDateTime.now())
                .build();
    }
}
