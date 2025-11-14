package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.UserRegistrationRequest; // Importe o novo DTO
import com.marketplace.marketplace.repository.OrganizationRepository;
import com.marketplace.marketplace.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

// APAGUE A CLASSE INTERNA UserRegistrationRequest se ela estava aqui

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          OrganizationRepository organizationRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public User registerUser(@RequestBody UserRegistrationRequest request) {
        User user = new User();

        // --- CORREÇÃO AQUI: Use os getters ---
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            user.setRole(com.marketplace.marketplace.domain.Role.valueOf("ROLE_" + request.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role inválida. Use ADMIN, GERENTE ou USUARIO.");
        }

        if (user.getRole() == com.marketplace.marketplace.domain.Role.ROLE_GERENTE) {
            if (request.getOrganizationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GERENTE precisa de um organizationId.");
            }
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada."));
            user.setOrganization(org);
        }
        // --- FIM DA CORREÇÃO ---

        return userRepository.save(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}