package com.audit.application.internal;

import com.audit.domain.exceptions.ExportLimitExceededException;

public class ExportConstraints {
    
    public static final int MAX_PDF_RECORDS = 2000;
    public static final int MAX_EXCEL_RECORDS = 50000;

    public static String resolveOptimalFormat(long recordCount) {
        if (recordCount <= MAX_PDF_RECORDS) return "PDF";
        if (recordCount <= MAX_EXCEL_RECORDS) return "EXCEL";
        throw new ExportLimitExceededException(
            "Record count " + recordCount + " exceeds maximum exportable limit of " + MAX_EXCEL_RECORDS);
    }

    public static boolean canExportAsPdf(long recordCount) {
        return recordCount <= MAX_PDF_RECORDS;
    }

    public static boolean canExportAsExcel(long recordCount) {
        return recordCount <= MAX_EXCEL_RECORDS;
    }

    public static void validateExportable(long recordCount, String format) {
        int max = "PDF".equals(format) ? MAX_PDF_RECORDS : MAX_EXCEL_RECORDS;
        if (recordCount > max) {
            throw new ExportLimitExceededException(
                "Cannot export " + recordCount + " records as " + format +
                ". Maximum: " + max);
        }
    }
}
