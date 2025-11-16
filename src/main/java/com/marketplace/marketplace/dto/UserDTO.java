package com.marketplace.marketplace.dto;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;

import java.util.UUID;

public class UserDTO {

    private UUID id;
    private String username;
    private Role role;
    private OrganizationDTO organization;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();

        if (user.getOrganization() != null) {
            this.organization = new OrganizationDTO(user.getOrganization());
        } else {
            this.organization = null;
        }

    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public OrganizationDTO getOrganization() { return organization; }


    private static class OrganizationDTO {
        private UUID id;
        private String name;

        public OrganizationDTO(Organization org) {
            this.id = org.getId();
            this.name = org.getName();
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
    }
}

