package com.marketplace.marketplace.config;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantFilterService {

    private final EntityManager entityManager;

    public TenantFilterService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void enableTenantFilter(UUID organizationId) {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("organizationId", organizationId);
    }

    public void disableTenantFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("tenantFilter");
    }
}