package com.marketplace.marketplace.config;

import com.marketplace.marketplace.repository.UserRepository;
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
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF
                .httpBasic(Customizer.withDefaults()) // Mantém o login do Postman

                .authorizeHttpRequests(authorize -> authorize
                        // 1. Acessos Públicos
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/register",        // Página de registro (GET)
                                "/register/salvar"  // Ação de registro (POST)
                        ).permitAll()

                        // 2. Acessos Autenticados (Qualquer Role)
                        .requestMatchers(
                                "/",
                                "/produto",
                                "/cart",
                                "/cart/**",
                                "/checkout",
                                "/checkout/**",
                                "/order-success"
                        ).authenticated()

                        // 3. Acessos de Gerente/Admin (Gerenciamento de Produtos pela Web)
                        .requestMatchers(
                                "/produto/salvar",
                                "/produto/editar/**",
                                "/produto/deletar/**"
                        ).hasAnyRole("GERENTE", "ADMIN")

                        // 4. Acessos SÓ DE ADMIN (Páginas Web de Admin)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 5. Acessos SÓ DE ADMIN (APIs de Admin)
                        .requestMatchers(
                                "/api/users/**",    // API de criar/listar usuários
                                "/api/org/**"       // API de criar/listar ONGs
                        ).hasRole("ADMIN")

                        // 6. API de Produtos (Gerente/Admin)
                        .requestMatchers(HttpMethod.POST, "/api/product").hasAnyRole("GERENTE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/product/**").hasAnyRole("GERENTE", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasAnyRole("GERENTE", "ADMIN")

                        // 7. Qualquer outra coisa, precisa estar logado
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                );
        return http.build();
    }
}