package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.ProductCategory;
import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.AiSearchResult;
import com.marketplace.marketplace.dto.ProductDTO;
import com.marketplace.marketplace.repository.OrganizationRepository;
import com.marketplace.marketplace.repository.ProductRepository;
import com.marketplace.marketplace.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/produto")
public class ProdutoViewController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoViewController.class);

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ProductService productService;

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return (User) authentication.getPrincipal();
    }

    @GetMapping
    public String listarProdutos(
            @RequestParam(required = false) String aiQuery,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            Model model) {

        User user = getLoggedInUser();

        if (aiQuery != null && !aiQuery.isEmpty()) {
            log.info("Controller: Iniciando busca por IA para: {}", aiQuery);
            AiSearchResult result = productService.aiSearch(aiQuery);
            model.addAttribute("produtos", result.getProducts());
            model.addAttribute("aiMessage", result.getFriendlyMessage());
            model.addAttribute("aiQuery", aiQuery);
        } else {
            log.info("Controller: Iniciando busca por filtros manuais.");
            List<ProductDTO> produtosDTO = productService.searchProducts(name, minPrice, maxPrice, category, sort);
            model.addAttribute("produtos", produtosDTO);

            model.addAttribute("filterName", name);
            model.addAttribute("filterMinPrice", minPrice);
            model.addAttribute("filterMaxPrice", maxPrice);
            model.addAttribute("filterCategory", category);
            model.addAttribute("filterSort", sort);
        }

        if (user.getRole() == Role.ROLE_ADMIN) {
            model.addAttribute("organizations", organizationRepository.findAll());
        }
        model.addAttribute("produto", new Product());

        model.addAttribute("allCategories", ProductCategory.values());

        return "produtos";
    }

    @GetMapping("/filter")
    public String filterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            Model model) {

        List<ProductDTO> produtosDTO = productService.searchProducts(name, minPrice, maxPrice, category, sort);
        model.addAttribute("produtos", produtosDTO);

        model.addAttribute("allCategories", ProductCategory.values());

        return "produtos :: #product-list-container";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@ModelAttribute Product produto) {
        User user = getLoggedInUser();

        if (user.getRole() == Role.ROLE_GERENTE) {
            produto.setOrganization(user.getOrganization());
        } else if (user.getRole() == Role.ROLE_ADMIN) {
            if (produto.getId() == null && (produto.getOrganization() == null || produto.getOrganization().getId() == null)) {
                return "redirect:/produto?error=admin_needs_org";
            }
        }

        productRepository.save(produto);
        return "redirect:/produto";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable("id") UUID id, Model model) {
        User user = getLoggedInUser();

        Product produto = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado ou inacessível"));

        if (user.getRole() == Role.ROLE_GERENTE) {
            Organization userOrg = user.getOrganization();
            if (produto.getOrganization() == null || !produto.getOrganization().getId().equals(userOrg.getId())) {
                return "redirect:/produto?error=permission";
            }
        }

        List<Product> produtosEntidades = productRepository.findAll();
        List<ProductDTO> produtosDTO = produtosEntidades.stream().map(ProductDTO::new).toList();

        if (user.getRole() == Role.ROLE_ADMIN) {
            model.addAttribute("organizations", organizationRepository.findAll());
        }

        model.addAttribute("allCategories", ProductCategory.values());

        model.addAttribute("produto", produto);
        model.addAttribute("produtos", produtosDTO);
        return "produtos";
    }

    @GetMapping("/deletar/{id}")
    public String deletarProduto(@PathVariable("id") UUID id) {
        User user = getLoggedInUser();

        Product produto = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado ou inacessível"));

        if (user.getRole() == Role.ROLE_GERENTE) {
            Organization userOrg = user.getOrganization();
            if (produto.getOrganization() == null || !produto.getOrganization().getId().equals(userOrg.getId())) {
                return "redirect:/produto?error=permission";
            }
        }

        productRepository.deleteById(id);
        return "redirect:/produto";
    }
}