package com.audit.infrastructure.adapters.input.security.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.audit.infrastructure.adapters.output.multitenancy.utils.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long enterpriseId = jwt.getClaim("enterprise_id");
            String userId = jwt.getSubject();
            String role = extractRole(authentication);
            log.debug("User {} with role {} from enterprise {} accessing audit",
                    userId, role, enterpriseId);
            if (enterpriseId != null) {
                TenantContext.setTenantId(String.valueOf(enterpriseId));
            }
            // Guardar contexto de usuario en request attributes para uso posterior
            request.setAttribute("audit.user.role", role);
            request.setAttribute("audit.user.enterpriseId", enterpriseId);
            request.setAttribute("audit.user.id", userId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5)) // Quitar prefijo "ROLE_"
            .findFirst()
            .orElse("ESTUDIANTE"); // Default
    }
}
