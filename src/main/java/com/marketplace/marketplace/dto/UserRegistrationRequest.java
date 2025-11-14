package com.marketplace.marketplace.dto;

import lombok.Data;
import java.util.UUID;

public class UserRegistrationRequest {

    private String username;
    private String password;
    private String role;
    private UUID organizationId;

    public UserRegistrationRequest() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UserRegistrationRequest(String username, String password, String role, UUID organizationId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.organizationId = organizationId;
    }


}