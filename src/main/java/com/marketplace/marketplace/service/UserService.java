package com.marketplace.marketplace.service;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.UserDTO;
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
import java.util.stream.Collectors;

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

    public UserDTO registerUser(UserRegistrationRequest request) {
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

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }


    public List<UserDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    public UserDTO findUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return new UserDTO(user);
    }

    private User findUserEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
    public UserDTO updateUser(UUID id, UserUpdateDto updateDto) {
        User user = findUserEntityById(id);
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

        User updatedUser = userRepository.save(user);
        return new UserDTO(updatedUser);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        userRepository.deleteById(id);
    }
}