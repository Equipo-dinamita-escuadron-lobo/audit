package com.audit.infrastructure.adapters.output.multitenancy.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.audit.infrastructure.adapters.input.messageBroker.aspect.JwtTokenService;
import com.audit.infrastructure.adapters.output.multitenancy.utils.TenantContext;

/**
 * @brief Interceptor for managing tenant context in HTTP requests
 * 
 * Extracts tenant identifier from JWT tokens and sets it in the TenantContext
 * for the duration of the request processing.
 */
@Component
public class TenantInterceptor implements WebRequestInterceptor {

    @Autowired
    private JwtTokenService jwtTokenService;

    /**
     * @brief Sets tenant context before controller execution
     * @param request The web request being processed
     * @throws Exception If tenant context cannot be established from JWT
     */
    @Override
    public void preHandle(WebRequest request) throws Exception {
        try {
            String tenantId = jwtTokenService.getTenantId();
            TenantContext.setTenantId(tenantId);
        } catch (Exception e) {
            // In case of error, do not set tenant context
            // This allows the application to work without tenant context if necessary
            throw new Exception("Could not establish tenant context from JWT", e);
        }
    }

    /**
     * @brief Clears tenant context after controller execution
     * @param request The web request being processed
     * @param model Model map (unused)
     */
    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        TenantContext.clear();
    }

    /**
     * @brief Final cleanup after request completion
     * @param request The web request being processed
     * @param ex Exception thrown by controller, if any
     */
    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        // No additional cleanup required
    }
}