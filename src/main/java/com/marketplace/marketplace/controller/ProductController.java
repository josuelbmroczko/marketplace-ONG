package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.AiSearchResult;
import com.marketplace.marketplace.dto.ProductDTO;
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

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping
    public ResponseEntity<?> getProducts( // <-- MUDOU AQUI
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