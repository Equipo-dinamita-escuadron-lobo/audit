package com.audit.infrastructure.adpaters.output.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.audit.infrastructure.adapters.output.exception.security.AuthenticationRequiredException;
import com.audit.infrastructure.adapters.output.exception.security.InvalidJwtAuthenticationException;
import com.audit.infrastructure.adapters.output.security.SecurityContextService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContextServiceTest {

    private final SecurityContextService service = new SecurityContextService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims);
    }

    @Test
    @DisplayName("getCurrentUserId - debe extraer sub desde JWT")
    void getCurrentUserId_withJwt_shouldReturnSub() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of(
                        "sub", "USER-001",
                        "preferred_username", "freider")),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String userId = service.getCurrentUserId();

        assertEquals("USER-001", userId);
    }

    @Test
    @DisplayName("getCurrentUserId - debe fallar si no hay autenticación")
    void getCurrentUserId_withoutAuthentication_shouldThrowException() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationRequiredException.class, service::getCurrentUserId);
    }

    @Test
    @DisplayName("getCurrentUserId - debe fallar si autenticación no es JWT")
    void getCurrentUserId_nonJwtAuthentication_shouldThrowException() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "user",
                "password",
                "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(InvalidJwtAuthenticationException.class, service::getCurrentUserId);
    }

    @Test
    @DisplayName("getCurrentUserRole - debe extraer primer rol sin prefijo ROLE_")
    void getCurrentUserRole_withRole_shouldReturnRoleWithoutPrefix() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of("sub", "USER-001")),
                List.of(
                        new SimpleGrantedAuthority("SCOPE_read"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = service.getCurrentUserRole();

        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("getCurrentUserRole - debe fallar si no hay rol ROLE_")
    void getCurrentUserRole_withoutRole_shouldThrowException() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of("sub", "USER-001")),
                List.of(new SimpleGrantedAuthority("SCOPE_read")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(InvalidJwtAuthenticationException.class, service::getCurrentUserRole);
    }

    @Test
    @DisplayName("getCurrentUsername - debe extraer preferred_username desde JWT")
    void getCurrentUsername_withPreferredUsername_shouldReturnUsername() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of(
                        "sub", "USER-001",
                        "preferred_username", "freider")),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = service.getCurrentUsername();

        assertEquals("freider", username);
    }

    @Test
    @DisplayName("getCurrentUsername - si no existe preferred_username debe usar sub")
    void getCurrentUsername_withoutPreferredUsername_shouldUseSub() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of("sub", "USER-001")),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = service.getCurrentUsername();

        assertEquals("USER-001", username);
    }

    @Test
    @DisplayName("getCurrentUsername - con autenticación no JWT debe retornar authentication name")
    void getCurrentUsername_nonJwtAuthentication_shouldReturnAuthenticationName() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "plain-user",
                "password",
                "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = service.getCurrentUsername();

        assertEquals("plain-user", username);
    }

    @Test
    @DisplayName("getJwtToken - debe retornar token JWT")
    void getJwtToken_withJwt_shouldReturnTokenValue() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of("sub", "USER-001")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("token-value", service.getJwtToken());
    }

    @Test
    @DisplayName("getJwtToken - debe fallar si no hay JWT")
    void getJwtToken_withoutJwt_shouldThrowException() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "plain-user",
                "password",
                "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(InvalidJwtAuthenticationException.class, service::getJwtToken);
    }

    @Test
    @DisplayName("extractClaim - debe extraer claim existente")
    void extractClaim_existingClaim_shouldReturnValue() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of(
                        "sub", "USER-001",
                        "enterprise_id", "ENT-001")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String claim = service.extractClaim("enterprise_id");

        assertEquals("ENT-001", claim);
    }

    @Test
    @DisplayName("extractClaim - claim inexistente debe retornar null")
    void extractClaim_missingClaim_shouldReturnNull() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of("sub", "USER-001")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String claim = service.extractClaim("enterprise_id");

        assertNull(claim);
    }

    @Test
    @DisplayName("extractClaim - debe fallar si autenticación no es JWT")
    void extractClaim_nonJwtAuthentication_shouldThrowException() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "plain-user",
                "password",
                "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(InvalidJwtAuthenticationException.class, () -> service.extractClaim("sub"));
    }

    @Test
    @DisplayName("getTokenWithBearer - debe retornar token con prefijo Bearer")
    void getTokenWithBearer_withJwt_shouldReturnBearerToken() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt(Map.of("sub", "USER-001")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("Bearer token-value", service.getTokenWithBearer());
    }
}
