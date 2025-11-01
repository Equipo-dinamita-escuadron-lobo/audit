package com.audit.infrastructure.adapters.output.exception.handler;

import com.audit.domain.exceptions.AuditDomainException;
import com.audit.domain.exceptions.InvalidAuditEventException;
import com.audit.domain.exceptions.InvalidTimestampException;
import com.audit.domain.exceptions.InvalidUserDataException;
import com.audit.infrastructure.adapters.output.exception.dto.ErrorResponseDto;

import java.nio.file.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUserDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleInvalidUserDataException(
        InvalidUserDataException ex,
        WebRequest request
    ) {
        log.error("Invalid user data: {}", ex.getMessage());
        return ErrorResponseDto.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();
    }

    @ExceptionHandler(InvalidAuditEventException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleInvalidAuditEventException(
            InvalidAuditEventException ex,
            WebRequest request) {

        log.error("Invalid audit event: {}", ex.getMessage());

        return ErrorResponseDto.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();
    }

    @ExceptionHandler(InvalidTimestampException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleInvalidTimeStampException(
            InvalidTimestampException ex,
            WebRequest request) {

        log.error("Invalid timestamp: {}", ex.getMessage());

        return ErrorResponseDto.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();
    }

    @ExceptionHandler(AuditDomainException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleAuditDomainException(
            AuditDomainException ex,
            WebRequest request) {

        log.error("Domain exception: {}", ex.getMessage(), ex);

        return ErrorResponseDto.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An error occurred processing your request")
            .path(getPath(request))
            .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        return ErrorResponseDto.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Request validation failed")
            .path(getPath(request))
            .validationErrors(validationErrors)
            .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDto handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        log.error("Access denied: {}", ex.getMessage());

        return ErrorResponseDto.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message("You don't have permission to access this resource")
            .path(getPath(request))
            .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleIllegalArgumentException(
        IllegalArgumentException ex,
        WebRequest request
    ){
        log.error("Illegal argument: {}", ex.getMessage());
        return ErrorResponseDto.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .path(getPath(request))
            .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleGlobalException(
        Exception ex, 
        WebRequest request
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ErrorResponseDto.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error ocurred. Please try again later")
            .path(getPath(request))
            .build();
    }
    
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
