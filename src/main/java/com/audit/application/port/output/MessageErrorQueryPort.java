package com.audit.application.port.output;

import java.util.Optional;

import com.audit.application.dto.response.MessageErrorResponse;
import com.audit.application.dto.response.PageResponse;

public interface MessageErrorQueryPort {

    Optional<MessageErrorResponse> findLatest();

    PageResponse<MessageErrorResponse> findAll(int page, int size);

    void deleteAll();
}
