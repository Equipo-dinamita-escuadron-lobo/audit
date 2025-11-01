package com.audit.application.dto.responses;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportSessionsResponse {
    private byte[] fileContent;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private int totalRecords; 
    private String format;
    private ZonedDateTime generatedAt;

    public static ExportSessionsResponse excel(byte[] content, String fileName, int recordCount) {
        return ExportSessionsResponse.builder()
            .fileContent(content)
            .fileName(fileName)
            .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .fileSize((long) content.length)
            .totalRecords(recordCount)
            .format("EXCEL")
            .generatedAt(ZonedDateTime.now())
            .build();
    }
    
    public static ExportSessionsResponse pdf(byte[] content, String fileName, int recordCount) {
        return ExportSessionsResponse.builder()
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
