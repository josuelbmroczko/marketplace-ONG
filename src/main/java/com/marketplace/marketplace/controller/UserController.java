package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.dto.UserDTO;
import com.marketplace.marketplace.dto.UserRegistrationRequest;
import com.marketplace.marketplace.dto.UserUpdateDto;
import com.marketplace.marketplace.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 2. MODIFICADO: Retorna ResponseEntity<UserDTO>
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserRegistrationRequest request) {
        UserDTO newUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    // 3. MODIFICADO: Retorna ResponseEntity<List<UserDTO>>
    // Esta é a correção principal para o seu problema da tabela vazia
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // 4. MODIFICADO: Retorna ResponseEntity<UserDTO>
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") UUID id) {
        UserDTO user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable("id") UUID id,
            @RequestBody UserUpdateDto updateDto) {

        UserDTO updatedUser = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}