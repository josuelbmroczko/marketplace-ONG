package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.repository.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/org")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @PostMapping
    public Organization createOrganization(@RequestBody Organization organization) {
        // Simplesmente salva a organização vinda do Postman
        return organizationRepository.save(organization);
    }

    @GetMapping
    public List<Organization> listOrganizations() {
        return organizationRepository.findAll();
    }
    @GetMapping("/{id}")
    public Organization getOrganizationById(@PathVariable("id") UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada"));
    }
}