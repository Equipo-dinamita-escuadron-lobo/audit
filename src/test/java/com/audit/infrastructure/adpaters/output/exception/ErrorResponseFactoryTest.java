package com.audit.infrastructure.adpaters.output.exception;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.audit.infrastructure.adapters.output.exception.dto.ErrorResponseDto;
import com.audit.infrastructure.adapters.output.exception.handler.ErrorResponseFactory;

class ErrorResponseFactoryTest {

    @Test
    @DisplayName("build - debe construir respuesta con todos los campos correctos")
    void build_validParams_shouldReturnCorrectResponse() {
        ErrorResponseDto result = ErrorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Something went wrong",
                "/api/test");

        assertAll(
                () -> assertNotNull(result.getTimestamp()),
                () -> assertEquals(400, result.getStatus()),
                () -> assertEquals("Bad Request", result.getError()),
                () -> assertEquals("Something went wrong", result.getMessage()),
                () -> assertEquals("/api/test", result.getPath()),
                () -> assertNull(result.getValidationErrors()));
    }

    @Test
    @DisplayName("build - debe funcionar con distintos HttpStatus")
    void build_differentStatus_shouldReflectCorrectCode() {
        ErrorResponseDto result = ErrorResponseFactory.build(
                HttpStatus.NOT_FOUND, "Not Found", "Resource missing", "/api/x");

        assertEquals(404, result.getStatus());
        assertEquals("Not Found", result.getError());
    }

    @Test
    @DisplayName("buildValidation - debe construir respuesta 400 con mapa de errores")
    void buildValidation_withErrors_shouldReturnValidationResponse() {
        Map<String, String> errors = Map.of("field1", "must not be null", "field2", "invalid value");

        ErrorResponseDto result = ErrorResponseFactory.buildValidation(
                "Validation Failed", "/api/test", errors);

        assertAll(
                () -> assertNotNull(result.getTimestamp()),
                () -> assertEquals(400, result.getStatus()),
                () -> assertEquals("Validation Failed", result.getError()),
                () -> assertEquals("Validation Failed", result.getMessage()),
                () -> assertEquals("/api/test", result.getPath()),
                () -> assertNotNull(result.getValidationErrors()),
                () -> assertEquals(2, result.getValidationErrors().size()),
                () -> assertEquals("must not be null", result.getValidationErrors().get("field1")));
    }

    @Test
    @DisplayName("buildValidation - mapa vacío debe incluirse igualmente")
    void buildValidation_emptyErrors_shouldStillBuild() {
        ErrorResponseDto result = ErrorResponseFactory.buildValidation(
                "Validation Failed", "/api/test", Collections.emptyMap());

        assertNotNull(result);
        assertEquals(400, result.getStatus());
        assertNotNull(result.getValidationErrors());
        assertTrue(result.getValidationErrors().isEmpty());
    }
}
