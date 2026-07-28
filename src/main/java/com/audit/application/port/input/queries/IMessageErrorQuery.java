package com.audit.application.port.input.queries;

import java.util.Optional;

import com.audit.application.dto.response.MessageErrorResponse;
import com.audit.application.dto.response.PageResponse;

public interface IMessageErrorQuery {

    Optional<MessageErrorResponse> findLatest();

    PageResponse<MessageErrorResponse> findAll(int page, int size);

    void deleteAll();
}
