package com.marketplace.marketplace.service;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.UserRegistrationRequest;
import com.marketplace.marketplace.dto.UserUpdateDto;
import com.marketplace.marketplace.repository.OrganizationRepository;
import com.marketplace.marketplace.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            user.setRole(Role.valueOf("ROLE_" + request.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role inválida. Use ADMIN, GERENTE ou USUARIO.");
        }

        if (user.getRole() == Role.ROLE_GERENTE) {
            if (request.getOrganizationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GERENTE precisa de um organizationId.");
            }
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada."));
            user.setOrganization(org);
        }
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public User updateUser(UUID id, UserUpdateDto updateDto) {
        User user = findUserById(id);

        user.setUsername(updateDto.getUsername());

        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        try {
            user.setRole(Role.valueOf("ROLE_" + updateDto.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role inválida.");
        }

        if (user.getRole() == Role.ROLE_GERENTE) {
            if (updateDto.getOrganizationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GERENTE precisa de um organizationId.");
            }
            Organization org = organizationRepository.findById(updateDto.getOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada."));
            user.setOrganization(org);
        } else {
            user.setOrganization(null);
        }

        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        userRepository.deleteById(id);
    }
}