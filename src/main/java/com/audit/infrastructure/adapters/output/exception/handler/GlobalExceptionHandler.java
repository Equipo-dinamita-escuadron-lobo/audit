package com.audit.infrastructure.adapters.output.exception.handler;

import com.audit.domain.exceptions.AuditDomainException;
import com.audit.domain.exceptions.ExportAuditException;
import com.audit.domain.exceptions.InvalidAuditEventException;

import com.audit.infrastructure.adapters.output.exception.dto.ErrorResponseDto;
import com.audit.infrastructure.adapters.output.exception.security.AuditMappingException;
import com.audit.infrastructure.adapters.output.exception.security.AuthenticationRequiredException;
import com.audit.infrastructure.adapters.output.exception.security.DuplicateResourceException;
import com.audit.infrastructure.adapters.output.exception.security.EntityNotFoundException;
import com.audit.infrastructure.adapters.output.exception.security.InvalidJwtAuthenticationException;
import com.audit.infrastructure.adapters.output.exception.security.MissingHeaderException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.BindException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(InvalidAuditEventException.class)
        public ResponseEntity<ErrorResponseDto> handleInvalidAuditEventException(
                        InvalidAuditEventException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Invalid Audit Event",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        @ExceptionHandler(BindException.class)
        public ResponseEntity<ErrorResponseDto> handleBindException(
                        BindException ex,
                        WebRequest request) {

                Map<String, String> validationErrors = new HashMap<>();

                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

                ErrorResponseDto response = ErrorResponseFactory.buildValidation(
                                "Invalid request parameters",
                                getPath(request),
                                validationErrors);

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(AuditDomainException.class)
        public ResponseEntity<ErrorResponseDto> handleAuditDomainException(
                        AuditDomainException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                "Business Rule Violation",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {
                Map<String, String> validationErrors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        validationErrors.put(fieldName, errorMessage);
                });

                ErrorResponseDto error = ErrorResponseFactory.buildValidation(
                                "Validation Failed",
                                getPath(request),
                                validationErrors);

                return ResponseEntity.badRequest().body(error);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
                        ConstraintViolationException ex,
                        WebRequest request) {

                ErrorResponseDto response = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Constraint Violation",
                                ex.getMessage(),
                                getPath(request));

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
                        AccessDeniedException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.FORBIDDEN,
                                "Forbidden",
                                "You don't have permission to access this resource",
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Bad Request",
                                ex.getMessage(),
                                getPath(request));

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDto> handleGlobalException(
                        Exception ex,
                        WebRequest request) {
                log.error("Unexpected error occurred", ex);
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal Server Error",
                                "An unexpected error occurred. Please try again later.",
                                getPath(request));

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
        }

        private String getPath(WebRequest request) {
                return request.getDescription(false).replace("uri=", "");
        }

        @ExceptionHandler(MissingHeaderException.class)
        public ResponseEntity<ErrorResponseDto> handleMissingHeaderException(MissingHeaderException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Bad Request",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(AuthenticationRequiredException.class)
        public ResponseEntity<ErrorResponseDto> handleAuthenticationRequired(
                        AuthenticationRequiredException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNAUTHORIZED,
                                "Authentication Required",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponseDto> handleResponseStatusException(
                        ResponseStatusException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.valueOf(ex.getStatusCode().value()),
                                HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase(),
                                ex.getReason(),
                                getPath(request));

                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        @ExceptionHandler(InvalidJwtAuthenticationException.class)
        public ResponseEntity<ErrorResponseDto> handleInvalidJwt(
                        InvalidJwtAuthenticationException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNAUTHORIZED,
                                "Unauthorized",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(ExportAuditException.class)
        public ResponseEntity<ErrorResponseDto> handleExportException(
                        ExportAuditException ex, WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Export Error",
                                "Error generating export file. Please try again.",
                                getPath(request));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Bad Request",
                                String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                                                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName()),
                                getPath(request));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex,
                        WebRequest request) {
                String message = "Invalid request body";
                Throwable cause = ex.getCause();
                if (cause instanceof InvalidFormatException invalidFormat) {
                        if (invalidFormat.getTargetType().isEnum()) {
                                message = "Invalid value for enum field";
                        }
                }
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Bad Request",
                                message,
                                getPath(request));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponseDto> handleEntityNotFound(
                        EntityNotFoundException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.NOT_FOUND,
                                "Resource Not Found",
                                ex.getMessage(),
                                getPath(request));

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponseDto> handleDuplicateResource(
                        DuplicateResourceException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.CONFLICT,
                                "Resource Conflict",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex,
                        WebRequest request) {

                String message = String.format(
                                "HTTP method '%s' is not supported for this endpoint",
                                ex.getMethod());

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.METHOD_NOT_ALLOWED,
                                "Method Not Allowed",
                                message,
                                getPath(request));

                return ResponseEntity
                                .status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(error);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponseDto> handleMediaTypeNotSupported(
                        HttpMediaTypeNotSupportedException ex,
                        WebRequest request) {
                String message = String.format(
                                "Content type '%s' is not supported",
                                ex.getContentType());
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                "Unsupported Media Type",
                                message,
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body(error);
        }

        @ExceptionHandler(ResourceAccessException.class)
        public ResponseEntity<ErrorResponseDto> handleResourceAccess(
                        ResourceAccessException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Service Unavailable",
                                "External service is temporarily unavailable",
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(error);
        }

        @ExceptionHandler(AuditMappingException.class)
        public ResponseEntity<ErrorResponseDto> handleAuditMappingException(
                        AuditMappingException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Audit Mapping Error",
                                ex.getMessage(),
                                getPath(request));

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
        }

        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<ErrorResponseDto> handleAuthorizationDenied(
                        AuthorizationDeniedException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.FORBIDDEN,
                                "Forbidden",
                                "You don't have permission to access this resource",
                                getPath(request));

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(error);
        }
}
