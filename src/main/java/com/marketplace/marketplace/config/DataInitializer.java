package com.marketplace.marketplace.config;

import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                System.out.println("Criando usuário ADMIN padrão...");

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setOrganization(null);

                userRepository.save(admin);
                System.out.println("Usuário ADMIN criado com sucesso!");
            }
        };
    }
}