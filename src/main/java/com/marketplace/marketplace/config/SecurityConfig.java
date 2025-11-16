package com.marketplace.marketplace.config;

import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autorizado");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso Negado");
                        })
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/", "/index.html", "/assets/**", "/error",
                                "/api/auth/login", "/api/auth/logout", "/api/auth/me",
                                "/api/register/salvar"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/product", "/api/product/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/product").hasAnyRole("GERENTE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/product/**").hasAnyRole("GERENTE", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasAnyRole("GERENTE", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/org", "/api/org/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/org").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/cart").hasAnyRole("USUARIO", "GERENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/cart/add/**").hasAnyRole("USUARIO", "GERENTE", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/remove/**").hasAnyRole("USUARIO", "GERENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/checkout/process").hasAnyRole("USUARIO", "GERENTE", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/register").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) -> {
                            User user = (User) authentication.getPrincipal();
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            String orgId = (user.getOrganization() != null) ? "\"" + user.getOrganization().getId().toString() + "\"" : "null";
                            response.getWriter().write(String.format(
                                    "{\"username\": \"%s\", \"role\": \"%s\", \"organizationId\": %s}",
                                    user.getUsername(),
                                    user.getRole().name(),
                                    orgId
                            ));
                        })
                        .failureHandler((request, response, exception) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário ou senha inválidos");
                        })
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}