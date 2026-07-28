package com.audit.infrastructure.adpaters.output.exception;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.context.request.WebRequest;
import com.audit.domain.enums.OperationType;
import com.audit.domain.exceptions.AuditDomainException;
import com.audit.domain.exceptions.ExportAuditException;
import com.audit.domain.exceptions.InvalidAuditCriteriaException;
import com.audit.domain.exceptions.InvalidAuditEventException;
import com.audit.infrastructure.adapters.output.exception.dto.ErrorResponseDto;
import com.audit.infrastructure.adapters.output.exception.handler.GlobalExceptionHandler;
import com.audit.infrastructure.adapters.output.exception.security.AuditMappingException;
import com.audit.infrastructure.adapters.output.exception.security.AuthenticationRequiredException;
import com.audit.infrastructure.adapters.output.exception.security.DuplicateResourceException;
import com.audit.infrastructure.adapters.output.exception.security.EntityNotFoundException;
import com.audit.infrastructure.adapters.output.exception.security.InvalidJwtAuthenticationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest request = mock(WebRequest.class);

    @BeforeEach
    void setUp() {
        when(request.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("handleInvalidAuditEventException - debe retornar 400 con mensaje de la excepción")
    void handleInvalidAuditEventException_shouldReturn400() {
        InvalidAuditEventException ex = new InvalidAuditEventException("Enterprise ID cannot be null");

        ResponseEntity<ErrorResponseDto> response = handler.handleInvalidAuditEventException(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(400, response.getBody().getStatus()),
                () -> assertEquals("Invalid Audit Event", response.getBody().getError()),
                () -> assertEquals("Enterprise ID cannot be null", response.getBody().getMessage()),
                () -> assertEquals("/api/test", response.getBody().getPath()));
    }

    @Test
    @DisplayName("handleAuditDomainException - debe retornar 422")
    void handleAuditDomainException_shouldReturn422() {
        AuditDomainException ex = new InvalidAuditCriteriaException("dateFrom cannot be after dateTo");

        ResponseEntity<ErrorResponseDto> response = handler.handleAuditDomainException(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode()),
                () -> assertEquals(422, response.getBody().getStatus()),
                () -> assertEquals("Business Rule Violation", response.getBody().getError()),
                () -> assertEquals("dateFrom cannot be after dateTo", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleExportException - debe retornar 500 con mensaje genérico")
    void handleExportException_shouldReturn500WithGenericMessage() {
        ExportAuditException ex = new ExportAuditException("job-123");

        ResponseEntity<ErrorResponseDto> response = handler.handleExportException(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals("Export Error", response.getBody().getError()),
                () -> assertEquals("Error generating export file. Please try again.",
                        response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleAuditMappingException - debe retornar 500 con mensaje de la excepción")
    void handleAuditMappingException_shouldReturn500() {
        AuditMappingException ex = new AuditMappingException("DocumentData cannot be null");

        ResponseEntity<ErrorResponseDto> response = handler.handleAuditMappingException(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals("Audit Mapping Error", response.getBody().getError()),
                () -> assertEquals("DocumentData cannot be null", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleAuthorizationDenied - debe retornar 403")
    void handleAuthorizationDenied_shouldReturn403() {
        AuthorizationDeniedException ex = new AuthorizationDeniedException("denied",
                new org.springframework.security.authorization.AuthorizationDecision(false));

        ResponseEntity<ErrorResponseDto> response = handler.handleAuthorizationDenied(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()),
                () -> assertEquals("Forbidden", response.getBody().getError()));
    }

    @Test
    @DisplayName("handleAuthenticationRequired - debe retornar 401")
    void handleAuthenticationRequired_shouldReturn401() {
        AuthenticationRequiredException ex = new AuthenticationRequiredException("No authenticated user found");

        ResponseEntity<ErrorResponseDto> response = handler.handleAuthenticationRequired(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertEquals("Authentication Required", response.getBody().getError()),
                () -> assertEquals("No authenticated user found", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleInvalidJwt - debe retornar 401")
    void handleInvalidJwt_shouldReturn401() {
        InvalidJwtAuthenticationException ex = new InvalidJwtAuthenticationException("Authentication is not JWT-based");

        ResponseEntity<ErrorResponseDto> response = handler.handleInvalidJwt(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertEquals("Unauthorized", response.getBody().getError()),
                () -> assertEquals("Authentication is not JWT-based", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleIllegalArgumentException - debe retornar 400 con mensaje de la excepción")
    void handleIllegalArgumentException_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("userName filter must be at least 3 characters");

        ResponseEntity<ErrorResponseDto> response = handler.handleIllegalArgumentException(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals("Bad Request", response.getBody().getError()),
                () -> assertEquals("userName filter must be at least 3 characters",
                        response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleConstraintViolation - debe retornar 400")
    void handleConstraintViolation_shouldReturn400() {
        ConstraintViolationException ex = new ConstraintViolationException("constraint violated",
                Collections.emptySet());

        ResponseEntity<ErrorResponseDto> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Constraint Violation", response.getBody().getError());
    }

    @Test
    @DisplayName("handleHttpMessageNotReadable - body inválido genérico debe retornar 400")
    void handleHttpMessageNotReadable_genericBody_shouldReturn400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Invalid body", (HttpInputMessage) null);

        ResponseEntity<ErrorResponseDto> response = handler.handleHttpMessageNotReadable(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals("Invalid request body", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleHttpMessageNotReadable - enum inválido debe retornar mensaje específico")
    void handleHttpMessageNotReadable_invalidEnum_shouldReturnEnumMessage() {
        InvalidFormatException invalidFormat = mock(InvalidFormatException.class);
        when(invalidFormat.getTargetType()).thenAnswer(inv -> OperationType.class);

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad enum", invalidFormat,
                (HttpInputMessage) null);

        ResponseEntity<ErrorResponseDto> response = handler.handleHttpMessageNotReadable(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals("Invalid value for enum field", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleTypeMismatch - debe retornar 400 con detalle del parámetro")
    void handleTypeMismatch_shouldReturn400WithDetail() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("INVALIDO");
        when(ex.getName()).thenReturn("operationType");
        when(ex.getRequiredType()).thenAnswer(inv -> OperationType.class);

        ResponseEntity<ErrorResponseDto> response = handler.handleTypeMismatch(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertTrue(response.getBody().getMessage().contains("INVALIDO")),
                () -> assertTrue(response.getBody().getMessage().contains("operationType")));
    }

    @Test
    @DisplayName("handleEntityNotFound - debe retornar 404 con mensaje de la excepción")
    void handleEntityNotFound_shouldReturn404() {
        EntityNotFoundException ex = new EntityNotFoundException("Audit record not found");

        ResponseEntity<ErrorResponseDto> response = handler.handleEntityNotFound(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals("Resource Not Found", response.getBody().getError()),
                () -> assertEquals("Audit record not found", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleDuplicateResource - debe retornar 409")
    void handleDuplicateResource_shouldReturn409() {
        DuplicateResourceException ex = new DuplicateResourceException("Resource already exists");

        ResponseEntity<ErrorResponseDto> response = handler.handleDuplicateResource(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals("Resource Conflict", response.getBody().getError()),
                () -> assertEquals("Resource already exists", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleMethodNotSupported - debe retornar 405 con método en el mensaje")
    void handleMethodNotSupported_shouldReturn405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ErrorResponseDto> response = handler.handleMethodNotSupported(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode()),
                () -> assertTrue(response.getBody().getMessage().contains("DELETE")));
    }

    @Test
    @DisplayName("handleMediaTypeNotSupported - debe retornar 415")
    void handleMediaTypeNotSupported_shouldReturn415() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(
                MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<ErrorResponseDto> response = handler.handleMediaTypeNotSupported(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode()),
                () -> assertTrue(response.getBody().getMessage().contains("text/plain")));
    }

    @Test
    @DisplayName("handleResourceAccess - debe retornar 503")
    void handleResourceAccess_shouldReturn503() {
        ResourceAccessException ex = new ResourceAccessException("Connection refused");

        ResponseEntity<ErrorResponseDto> response = handler.handleResourceAccess(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode()),
                () -> assertEquals("External service is temporarily unavailable",
                        response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleResponseStatusException - debe reflejar status de la excepción")
    void handleResponseStatusException_shouldReflectExceptionStatus() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.GONE, "Resource expired");

        ResponseEntity<ErrorResponseDto> response = handler.handleResponseStatusException(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.GONE, response.getStatusCode()),
                () -> assertEquals(410, response.getBody().getStatus()),
                () -> assertEquals("Resource expired", response.getBody().getMessage()));
    }

    @Test
    @DisplayName("handleGlobalException - excepción genérica debe retornar 500 con mensaje genérico")
    void handleGlobalException_shouldReturn500WithGenericMessage() {
        Exception ex = new RuntimeException("NullPointerException en algún servicio");

        ResponseEntity<ErrorResponseDto> response = handler.handleGlobalException(ex, request);

        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals("Internal Server Error", response.getBody().getError()),
                () -> assertEquals("An unexpected error occurred. Please try again later.",
                        response.getBody().getMessage()));
    }

    @Test
    @DisplayName("getPath - debe limpiar el prefijo uri= del WebRequest")
    void getPath_shouldStripUriPrefix() {
        when(request.getDescription(false)).thenReturn("uri=/api/audit/sessions");

        ResponseEntity<ErrorResponseDto> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("test"), request);

        assertEquals("/api/audit/sessions", response.getBody().getPath());
    }
}
