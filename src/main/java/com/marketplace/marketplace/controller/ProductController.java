package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.AiSearchResponse;
import com.marketplace.marketplace.dto.AiSearchResult;
import com.marketplace.marketplace.dto.ProductDTO;
import com.marketplace.marketplace.service.GeminiAiSearchService;
import com.marketplace.marketplace.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    private final GeminiAiSearchService aiSearchService;

    public ProductController(ProductService productService, GeminiAiSearchService aiSearchService) {
        this.productService = productService;
        this.aiSearchService = aiSearchService;
    }


    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String aiQuery
    ) {

        if (aiQuery != null && !aiQuery.isEmpty()) {

            AiSearchResult result = productService.aiSearch(aiQuery);
            return ResponseEntity.ok(result);

        } else {

            List<ProductDTO> products = productService.findWithFilters(name, minPrice, maxPrice, category, sort);
            return ResponseEntity.ok(products);
        }
    }

    @GetMapping("/test-ai")
    public ResponseEntity<?> testAi(@RequestParam String query) {
        System.out.println("\n\n--- INICIANDO TESTE DIRETO DA IA ---");
        try {
            AiSearchResponse response = aiSearchService.parseSearchQuery(query);
            System.out.println("--- TESTE DA IA BEM-SUCEDIDO ---");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("--- TESTE DA IA FALHOU: " + e.getMessage() + " ---");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao chamar IA: " + e.getMessage());
        }
    }


    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @RequestBody Product product,
            @AuthenticationPrincipal User loggedInUser) {

        ProductDTO newProduct = productService.createProduct(product, loggedInUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable UUID id,
            @RequestBody Product productDetails,
            @AuthenticationPrincipal User loggedInUser) {

        ProductDTO updatedProduct = productService.updateProduct(id, productDetails, loggedInUser);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedInUser) {

        productService.deleteProduct(id, loggedInUser);
        return ResponseEntity.noContent().build();
    }
}