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

    /**
     * Este Pointcut diz: "Executar este código ANTES de qualquer método
     * em qualquer classe dentro do pacote .repository"
     */
    @Before("execution(* com.marketplace.marketplace.repository.*.*(..))")
    public void applyTenantFilter() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Session session = entityManager.unwrap(Session.class);

        // 1. Desabilita o filtro por padrão (para Admin e anônimos)
        // Isto limpa o estado de requisições anteriores.
        session.disableFilter("tenantFilter");

        // 2. Se não há usuário logado (ex: /login, /register), o filtro fica desligado.
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return;
        }

        Object principal = authentication.getPrincipal();

        // 3. Verifica se o usuário logado é realmente uma instância de User
        if (principal instanceof User) {
            User user = (User) principal;

            // 4. Habilita o filtro APENAS SE o usuário NÃO FOR ADMIN
            if (user.getRole() != Role.ROLE_ADMIN) {

                if (user.getOrganization() != null) {
                    // É Gerente ou Usuário, e tem uma ONG. Ativa o filtro.
                    UUID organizationId = user.getOrganization().getId();
                    session.enableFilter("tenantFilter")
                            .setParameter("organizationId", organizationId);
                } else {
                    // Segurança: se não for admin E não tiver ONG, não retorna nada.
                    // (Define um ID de tenant que não existe)
                    session.enableFilter("tenantFilter")
                            .setParameter("organizationId", UUID.fromString("00000000-0000-0000-0000-000000000000"));
                }
            }
            // Se for ADMIN, o filtro permanece desligado (do passo 1).
        }
    }
}