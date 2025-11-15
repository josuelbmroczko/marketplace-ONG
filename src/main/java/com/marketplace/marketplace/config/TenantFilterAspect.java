package com.marketplace.marketplace.config;

import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class TenantFilterAspect {

    @Autowired
    private EntityManager entityManager;

    @Before("execution(* com.marketplace.marketplace.repository.*.*(..))")
    public void applyTenantFilter() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Session session = entityManager.unwrap(Session.class);

        session.disableFilter("tenantFilter");

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;

            if (user.getRole() != Role.ROLE_ADMIN) {

                if (user.getOrganization() != null) {
                    UUID organizationId = user.getOrganization().getId();
                    session.enableFilter("tenantFilter")
                            .setParameter("organizationId", organizationId);
                } else {

                    session.enableFilter("tenantFilter")
                            .setParameter("organizationId", UUID.fromString("00000000-0000-0000-0000-000000000000"));
                }
            }
        }
    }
}