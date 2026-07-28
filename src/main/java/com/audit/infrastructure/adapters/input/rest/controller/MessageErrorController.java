package com.audit.infrastructure.adapters.input.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.audit.application.dto.response.MessageErrorResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.port.input.queries.IMessageErrorQuery;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit/message-errors")
@RequiredArgsConstructor
public class MessageErrorController {

    private final IMessageErrorQuery messageErrorQuery;

    @GetMapping("/latest")
    public ResponseEntity<MessageErrorResponse> findLatest() {
        return messageErrorQuery.findLatest()
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No message processing errors found"));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MessageErrorResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(messageErrorQuery.findAll(page, size));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        messageErrorQuery.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
