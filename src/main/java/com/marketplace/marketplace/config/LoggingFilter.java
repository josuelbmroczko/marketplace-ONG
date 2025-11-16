package com.marketplace.marketplace.config;

import com.marketplace.marketplace.domain.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        long startTime = System.currentTimeMillis();
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                MDC.put("user_id", user.getId().toString());
                if (user.getOrganization() != null) {
                    MDC.put("organization_id", user.getOrganization().getId().toString());
                }
            }

            MDC.put("http_method", httpServletRequest.getMethod());
            MDC.put("http_path", httpServletRequest.getRequestURI());
            chain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            MDC.put("http_status", String.valueOf(httpServletResponse.getStatus()));
            MDC.put("duration_ms", String.valueOf(duration));

            MDC.clear();
        }
    }
}