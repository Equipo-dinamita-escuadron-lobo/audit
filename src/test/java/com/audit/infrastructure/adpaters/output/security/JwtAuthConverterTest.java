package com.audit.infrastructure.adpaters.output.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import com.audit.infrastructure.adapters.output.security.JwtAuthConverter;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthConverterTest {

        private Jwt jwt(Map<String, Object> claims) {
                return new Jwt(
                                "token-value",
                                Instant.now(),
                                Instant.now().plusSeconds(3600),
                                Map.of("alg", "none"),
                                claims);
        }

        private void setPrincipleAttribute(JwtAuthConverter converter, String value) throws Exception {
                Field field = JwtAuthConverter.class.getDeclaredField("principleAttribute");
                field.setAccessible(true);
                field.set(converter, value);
        }

        @Test
        @DisplayName("convert - debe extraer roles y permisos desde JWT")
        void convert_shouldExtractRolesAndPermissions() throws Exception {
                JwtAuthConverter converter = new JwtAuthConverter();
                setPrincipleAttribute(converter, "preferred_username");

                List<String> roles = new ArrayList<>();
                roles.add("Administrador");
                roles.add("Profesor");
                roles.add("");
                roles.add(null);

                Jwt jwt = jwt(Map.of(
                                "sub", "USER-001",
                                "preferred_username", "freider",
                                "realm_access", Map.of("roles", roles),
                                "authorization", Map.of(
                                                "permissions", List.of(
                                                                Map.of("rsname", "Export_Excel_Audit_Sessions"),
                                                                Map.of("rsname", "HC"),
                                                                Map.of("rsname", "")))));

                Authentication authentication = converter.convert(jwt);

                assertAll(
                                () -> assertEquals("freider", authentication.getName()),
                                () -> assertTrue(authentication.getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("ROLE_Administrador"))),
                                () -> assertTrue(authentication.getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("ROLE_Profesor"))),
                                () -> assertTrue(authentication.getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("Export_Excel_Audit_Sessions"))),
                                () -> assertTrue(authentication.getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("HC"))),
                                () -> assertFalse(authentication.getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("ROLE_"))));
        }

        @Test
        @DisplayName("convert - si principleAttribute es nulo debe usar sub")
        void convert_nullPrincipleAttribute_shouldUseSub() {
                JwtAuthConverter converter = new JwtAuthConverter();

                Jwt jwt = jwt(Map.of("sub", "USER-001"));

                Authentication authentication = converter.convert(jwt);

                assertEquals("USER-001", authentication.getName());
        }

        @Test
        @DisplayName("convert - sin realm_access ni authorization debe retornar autenticación sin roles personalizados")
        void convert_withoutRolesAndPermissions_shouldNotFail() {
                JwtAuthConverter converter = new JwtAuthConverter();

                Jwt jwt = jwt(Map.of("sub", "USER-001"));

                Authentication authentication = converter.convert(jwt);

                assertNotNull(authentication);
                assertEquals("USER-001", authentication.getName());
        }

        @Test
        @DisplayName("convert - authorization sin permissions debe retornar solo roles")
        void convert_authorizationWithoutPermissions_shouldReturnRolesOnly() {
                JwtAuthConverter converter = new JwtAuthConverter();

                Jwt jwt = jwt(Map.of(
                                "sub", "USER-001",
                                "realm_access", Map.of("roles", List.of("Administrador")),
                                "authorization", Map.of()));

                Authentication authentication = converter.convert(jwt);

                assertTrue(authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_Administrador")));
        }
}
