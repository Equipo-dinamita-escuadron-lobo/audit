package com.audit.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportEstimationResponse {
    private long estimatedRecords;
    private String recommendedFormat; 
    private int maxRecordsForPdf;
    private int maxRecordsForExcel;
    private int estimatedPdfPages;
    private boolean canExportAsPdf;
    private String reason; 
}
