package com.audit.application.dto.response;

import com.audit.domain.enums.ExportFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportFileResult {
    private final byte[] data;
    private final String fileName;
    private final ExportFormat format;
}
