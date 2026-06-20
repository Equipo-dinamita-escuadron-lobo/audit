package com.audit.infrastructure.adapters.output.security.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.audit.infrastructure.adapters.output.exception.dto.ErrorResponseDto;
import com.audit.infrastructure.adapters.output.exception.handler.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void handle(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AccessDeniedException accessDeniedException)
                        throws IOException {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.FORBIDDEN,
                                "Forbidden",
                                "You don't have permission to access this resource",
                                request.getRequestURI());

                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                response.getWriter().write(
                                objectMapper.writeValueAsString(error));
        }
}
