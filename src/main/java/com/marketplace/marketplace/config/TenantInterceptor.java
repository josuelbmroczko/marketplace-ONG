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

        // Pega a sessão do Hibernate
        Session session = entityManager.unwrap(Session.class);

        // 1. DESLIGA O FILTRO POR PADRÃO no início de CADA requisição.
        // Isto garante que o Admin (e páginas públicas) vejam tudo
        // e limpa o estado de requisições anteriores.
        session.disableFilter("tenantFilter");

        // 2. Se não há usuário logado (ex: /login), o filtro fica desligado.
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        User user = (User) authentication.getPrincipal();

        // 3. LIGA O FILTRO APENAS SE o usuário NÃO FOR ADMIN.
        if (user.getRole() != Role.ROLE_ADMIN) {

            if (user.getOrganization() != null) {
                // É Gerente ou Usuário, e tem uma ONG. Ativa o filtro.
                UUID organizationId = user.getOrganization().getId();
                session.enableFilter("tenantFilter")
                        .setParameter("organizationId", organizationId);
            } else {
                // NÃO é Admin E NÃO tem ONG (um estado inválido/perigoso).
                // Ativamos o filtro com um UUID "fantasma" (000...000)
                // para garantir que NENHUMA consulta retorne dados.
                session.enableFilter("tenantFilter")
                        .setParameter("organizationId", UUID.fromString("00000000-0000-0000-0000-000000000000"));
            }
        }

        // Se for Admin, o filtro simplesmente permanece desligado (passo 1)
        return true;
    }
}