package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.UserRegistrationRequest;
import com.marketplace.marketplace.repository.OrganizationRepository;
import com.marketplace.marketplace.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           OrganizationRepository organizationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public String showUserManagement(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("organizations", organizationRepository.findAll());
        model.addAttribute("newUserRequest", new UserRegistrationRequest());
        return "admin-users";
    }

    @PostMapping("/users/salvar")
    public String createUser(@ModelAttribute("newUserRequest") UserRegistrationRequest request) {

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            user.setRole(Role.valueOf("ROLE_" + request.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role inválida.");
        }

        if (user.getRole() == Role.ROLE_GERENTE) {
            if (request.getOrganizationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GERENTE precisa de um organizationId.");
            }
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada."));
            user.setOrganization(org);
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }
}