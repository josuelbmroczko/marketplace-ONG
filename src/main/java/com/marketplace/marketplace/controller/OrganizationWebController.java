package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.repository.OrganizationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/orgs")
public class OrganizationWebController {

    private final OrganizationRepository organizationRepository;

    public OrganizationWebController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @GetMapping
    public String showOrgManagement(Model model) {
        model.addAttribute("orgs", organizationRepository.findAll());
        model.addAttribute("newOrg", new Organization());
        return "admin-orgs";
    }

    @PostMapping("/salvar")
    public String saveOrg(@ModelAttribute("newOrg") Organization organization) {
        organizationRepository.save(organization);
        return "redirect:/admin/orgs";
    }
}