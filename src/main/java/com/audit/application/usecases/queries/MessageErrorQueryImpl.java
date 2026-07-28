package com.audit.application.usecases.queries;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.audit.application.dto.response.MessageErrorResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.IMessageErrorQuery;
import com.audit.application.port.output.MessageErrorQueryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageErrorQueryImpl implements IMessageErrorQuery {

    private final MessageErrorQueryPort messageErrorQueryPort;

    @Override
    public Optional<MessageErrorResponse> findLatest() {
        return messageErrorQueryPort.findLatest();
    }

    @Override
    public PageResponse<MessageErrorResponse> findAll(int page, int size) {
        return messageErrorQueryPort.findAll(page, size);
    }

    @Override
    public void deleteAll() {
        messageErrorQueryPort.deleteAll();
    }
}
