package com.marketplace.marketplace.config;

import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Session session = entityManager.unwrap(Session.class);

        session.disableFilter("tenantFilter");

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return true;
        }


        User user = (User) authentication.getPrincipal();

        if (user.getRole() == Role.ROLE_GERENTE) {

            if (user.getOrganization() != null) {
                UUID organizationId = user.getOrganization().getId();
                session.enableFilter("tenantFilter")
                        .setParameter("organizationId", organizationId);
            }
        }

        return true;
    }
}