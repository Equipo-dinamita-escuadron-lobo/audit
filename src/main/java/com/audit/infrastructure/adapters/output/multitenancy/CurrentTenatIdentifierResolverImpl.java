package com.audit.infrastructure.adapters.output.multitenancy;

import com.audit.infrastructure.adapters.output.multitenancy.utils.TenantContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * @brief Hibernate tenant identifier resolver for multi-tenant database access
 * 
 * Resolves the current tenant identifier from TenantContext and configures
 * Hibernate for multi-tenant operation.
 */
@SuppressWarnings("rawtypes")
@Component
class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    /**
     * @brief Resolves the current tenant identifier
     * @return Current tenant ID from TenantContext, or "BOOTSTRAP" if none available
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (!ObjectUtils.isEmpty(tenantId)) {
            return tenantId;
        } else {
            // Allow bootstrapping the EntityManagerFactory, in which case no tenant is
            // needed
            return "BOOTSTRAP";
        }
    }

    /**
     * @brief Validates existing current sessions
     * @return Always true, as current sessions are considered valid by default
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    /**
     * @brief Customizes Hibernate properties for multi-tenant configuration
     * @param hibernateProperties Properties map to customize
     */
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}