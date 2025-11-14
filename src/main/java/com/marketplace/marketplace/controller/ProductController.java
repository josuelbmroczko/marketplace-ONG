package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// import static org.springframework.data.jpa.domain.AbstractPersistable_.id; // Esta linha não é necessária

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private ProductRepository productRepository;
    public ProductController(ProductRepository productRepository){
        this.productRepository=productRepository;
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable("id") UUID id) {
        return productRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product){
        return productRepository.save(product);
    }
    @GetMapping
    public List<Product> listProduct(){
        return productRepository.findAll();
    }
    @PutMapping("/{id}")
    public void editProduct(@PathVariable("id") UUID id, @RequestBody Product product){
        product.setId(id);
        productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id")UUID id){
        productRepository.deleteById(id);
    }

    // GARANTA QUE A CLASSE UserRegistrationRequest NÃO ESTÁ AQUI DENTRO
}