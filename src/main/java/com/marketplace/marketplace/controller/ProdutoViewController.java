package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Controller
@RequestMapping("/produto")
public class ProdutoViewController {

    @Autowired
    private RestTemplate restTemplate;

    // 🔽 2. INJETE A PROPRIEDADE DO ARQUIVO .properties AQUI
    @Value("${api.base.url}")
    private String apiBaseUrl;

    // ✨ Vamos criar uma constante para o caminho específico do produto
    private final String PRODUCT_API_PATH = "/product";

    @GetMapping
    public String listarProdutos(Model model) {
        // 3. CONSTRUA A URL COMPLETA USANDO A VARIÁVEL
        String apiUrl = apiBaseUrl + PRODUCT_API_PATH;
        Product[] produtos = restTemplate.getForObject(apiUrl, Product[].class);

        model.addAttribute("produtos", produtos);
        model.addAttribute("produto", new Product());
        return "produtos";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@ModelAttribute Product produto) {
        String apiUrl = apiBaseUrl + PRODUCT_API_PATH;
        UUID id = produto.getId();
        if (id == null) {
            restTemplate.postForObject(apiUrl, produto, Product.class);
        } else {
            restTemplate.put(apiUrl + "/" + id, produto);
        }
        return "redirect:/produto";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable("id") UUID id, Model model) {
        String apiUrl = apiBaseUrl + PRODUCT_API_PATH;
        Product produto = restTemplate.getForObject(apiUrl + "/" + id, Product.class);
        Product[] produtos = restTemplate.getForObject(apiUrl, Product[].class);

        model.addAttribute("produto", produto);
        model.addAttribute("produtos", produtos);
        return "produtos";
    }

    @GetMapping("/deletar/{id}")
    public String deletarProduto(@PathVariable("id") UUID id) {
        String apiUrl = apiBaseUrl + PRODUCT_API_PATH;
        restTemplate.delete(apiUrl + "/" + id);
        return "redirect:/produto";
    }
}