package com.audit.infrastructure.adapters.output.security;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.lang.NonNull;

/**
 * Clase que implementa la conversión de un JWT en un token de autenticación.
 * También proporciona métodos utilitarios relacionados con JWT.
 */
@Component
@Slf4j
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttribute;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream
                .concat(
                        jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                        extractAuthorities(jwt).stream())
                .toList();

        return new JwtAuthenticationToken(jwt, authorities, getPrincipleName(jwt));
    }

    private String getPrincipleName(Jwt jwt) {
        String claimName = (principleAttribute != null) ? principleAttribute : JwtClaimNames.SUB;
        return jwt.getClaim(claimName);
    }

    /**
     * Extrae dos tipos de autoridades del JWT:
     * 1. Roles de realm_access.roles → "ROLE_ADMINISTRADOR", "ROLE_PROFESOR", etc.
     * Usados con hasAnyRole() en @PreAuthorize
     * 2. rsname de authorization.permissions → "Export_Excel_Audit_Sessions", "HC",
     * etc.
     * Usados con hasAuthority() en @PreAuthorize
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // --- 1. Roles de realm_access ---
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            for (String role : roles) {
                if (role != null && !role.isBlank()) {
                    // hasAnyRole("ADMINISTRADOR") busca "ROLE_ADMINISTRADOR"
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    log.debug("Role extraído: ROLE_{}", role);
                }
            }
        }

        // --- 2. Permisos de authorization.permissions (rsname) ---
        Map<String, Object> authorization = jwt.getClaim("authorization");
        if (authorization == null || !authorization.containsKey("permissions")) {
            return authorities;
        }

        List<Map<String, Object>> permissions = (List<Map<String, Object>>) authorization.get("permissions");

        for (Map<String, Object> permission : permissions) {
            Object rsnameObj = permission.get("rsname");
            if (!(rsnameObj instanceof String rsname) || rsname.isBlank()) {
                continue;
            }
            authorities.add(new SimpleGrantedAuthority(rsname));
            log.debug("Permiso extraído: {}", rsname);
        }

        return authorities;
    }
}

