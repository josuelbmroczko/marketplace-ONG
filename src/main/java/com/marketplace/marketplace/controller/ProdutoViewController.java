package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.repository.OrganizationRepository;
import com.marketplace.marketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/produto")
public class ProdutoViewController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return (User) authentication.getPrincipal();
    }

    @GetMapping
    public String listarProdutos(Model model) {
        User user = getLoggedInUser();

        // --- LÓGICA DE FILTRO REMOVIDA DAQUI ---
        // O `TenantInterceptor` agora cuida disso automaticamente.
        // `findAll()` já virá filtrado se o usuário não for ADMIN.
        List<Product> produtos = productRepository.findAll();
        // --- FIM DA ALTERAÇÃO ---

        // Adiciona a lista de ONGs apenas se for Admin (para o dropdown do formulário)
        if (user.getRole() == Role.ROLE_ADMIN) {
            model.addAttribute("organizations", organizationRepository.findAll());
        }

        model.addAttribute("produtos", produtos);
        model.addAttribute("produto", new Product());
        return "produtos";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@ModelAttribute Product produto) {
        User user = getLoggedInUser();

        if (user.getRole() == Role.ROLE_GERENTE) {
            // Gerente só pode salvar na sua própria ONG
            produto.setOrganization(user.getOrganization());

        } else if (user.getRole() == Role.ROLE_ADMIN) {
            // Admin precisa ter selecionado uma ONG no dropdown
            if (produto.getId() == null && (produto.getOrganization() == null || produto.getOrganization().getId() == null)) {
                return "redirect:/produto?error=admin_needs_org";
            }

            // Para edição, precisamos garantir que o ID da ONG seja pego do <select>
            // O th:field="*{organization}" no HTML já cuida disso,
            // pois ele binda o ID selecionado para 'produto.organization.id'.
        }

        productRepository.save(produto);
        return "redirect:/produto";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable("id") UUID id, Model model) {
        User user = getLoggedInUser();

        // O `findById` já é filtrado pelo TenantInterceptor.
        // Se um gerente tentar editar o ID de outra ONG, o 'findById' falhará.
        Product produto = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado ou inacessível"));

        // A verificação de segurança extra não é mais estritamente necessária
        // por causa do filtro, mas é uma boa prática manter.
        if (user.getRole() == Role.ROLE_GERENTE) {
            Organization userOrg = user.getOrganization();
            if (produto.getOrganization() == null || !produto.getOrganization().getId().equals(userOrg.getId())) {
                return "redirect:/produto?error=permission"; // Dupla garantia
            }
        }

        // `findAll()` aqui também será filtrado automaticamente
        List<Product> produtos = productRepository.findAll();

        if (user.getRole() == Role.ROLE_ADMIN) {
            model.addAttribute("organizations", organizationRepository.findAll());
        }

        model.addAttribute("produto", produto);
        model.addAttribute("produtos", produtos);
        return "produtos";
    }

    @GetMapping("/deletar/{id}")
    public String deletarProduto(@PathVariable("id") UUID id) {
        User user = getLoggedInUser();

        // findById já é filtrado. Gerente não pode deletar produto de outra ONG.
        Product produto = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado ou inacessível"));

        // Dupla garantia (opcional, mas bom)
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