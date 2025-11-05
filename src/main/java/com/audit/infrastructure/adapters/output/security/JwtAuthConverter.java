package com.audit.infrastructure.adapters.output.security;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
 * @brief JWT authentication converter for Spring Security
 * 
 * Converts JWT tokens to authentication objects with proper authority mapping
 * and principal extraction for OAuth2 resource server configuration.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken>, IJwtUtils {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAtrribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    Jwt jwtToken;

    /**
     * @brief Converts a JWT into an AbstractAuthenticationToken for authentication
     * @param jwt The JWT to convert
     * @return The corresponding AbstractAuthenticationToken
     */
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {

        Collection<GrantedAuthority> authorities = Stream
                .concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(), 
                        extractRealmRoles(jwt).stream())
                .toList();

        this.jwtToken = jwt;

        return new JwtAuthenticationToken(jwt, authorities, getPrincipleName(jwt));

    }

    /**
     * @brief Returns the authenticated user's name from the JWT
     * 
     *        By default, uses the "sub" claim from the JWT, but can be configured
     *        to use a different claim by setting the
     *        "jwt.auth.converter.principle-attribute" property.
     *
     * @param jwt The JWT from which to extract the username
     * @return The authenticated user's name
     */
    private String getPrincipleName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;

        if (principleAtrribute != null) {
            claimName = principleAtrribute;
        }

        return jwt.getClaim(claimName);
    }

    /**
     * @brief Extracts roles from the "resource_access" claim of the JWT
     * 
     *        Converts each role into a SimpleGrantedAuthority with "ROLE_" prefix.
     *        If the "resource_access" claim or specific resource ID or its roles
     *        are not present,
     *        returns an empty collection.
     * 
     * @param jwt The JWT from which to extract roles
     * @return A collection of GrantedAuthority representing the roles
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }

        Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");

        return realmRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())) // ← Convertir a mayúsculas
                .toList();
    }

    /**
     * @brief Returns the "sub" claim value from the JWT
     * @return The authenticated user's identifier
     */
    @Override
    public String getId() {
        return (String) jwtToken.getClaims().get("sub");
    }

    /**
     * @brief Returns the JWT token value as a string
     * @return The JWT token as a string
     */
    @Override
    public String getToken() {
        return jwtToken.getTokenValue();
    }

}
