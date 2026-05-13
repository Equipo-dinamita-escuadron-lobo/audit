package com.audit.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetDocumentEventDetailRequest {
    private String enterpriseId;
    private String documentCode;
}
