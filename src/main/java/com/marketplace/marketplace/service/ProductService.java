package com.marketplace.marketplace.service;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.Role;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.AiSearchResult;
import com.marketplace.marketplace.dto.ProductDTO;
import com.marketplace.marketplace.repository.OrganizationRepository;
import com.marketplace.marketplace.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;

    public ProductService(ProductRepository productRepository, OrganizationRepository organizationRepository) {
        this.productRepository = productRepository;
        this.organizationRepository = organizationRepository;
    }


    @Transactional
    public ProductDTO updateProduct(UUID productId, Product productDetails, User loggedInUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        if (loggedInUser.getRole() == Role.ROLE_GERENTE) {
            if (product.getOrganization() == null || !product.getOrganization().getId().equals(loggedInUser.getOrganization().getId())) {
                throw new AccessDeniedException("Gerente não tem permissão para editar este produto.");
            }
        }

        product.setProductName(productDetails.getProductName());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());
        product.setDescription(productDetails.getDescription());
        product.setAmout(productDetails.getAmout());

        if (loggedInUser.getRole() == Role.ROLE_ADMIN) {

        }

        Product savedProduct = productRepository.save(product);
        return new ProductDTO(savedProduct);
    }

    @Transactional
    public ProductDTO createProduct(Product product, User loggedInUser) {
        if (loggedInUser.getRole() == Role.ROLE_GERENTE) {
            product.setOrganization(loggedInUser.getOrganization());
        } else if (loggedInUser.getRole() == Role.ROLE_ADMIN) {
            if (product.getOrganization() == null || product.getOrganization().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin deve selecionar uma ONG para o produto.");
            }
            Organization org = organizationRepository.findById(product.getOrganization().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada"));
            product.setOrganization(org);
        }
        product.setOrderItems(null);

        Product savedProduct = productRepository.save(product);
        return new ProductDTO(savedProduct);
    }

    @Transactional
    public void deleteProduct(UUID productId, User loggedInUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        if (loggedInUser.getRole() == Role.ROLE_GERENTE) {
        }
        productRepository.delete(product);
    }


    public List<ProductDTO> findWithFilters(String name, BigDecimal minPrice, BigDecimal maxPrice, String category, String sort) {

        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    public AiSearchResult aiSearch(String aiQuery) {

        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(aiQuery);

        List<ProductDTO> productDTOs = products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());

        String message = "Busca por IA encontrou " + productDTOs.size() + " produtos para '" + aiQuery + "'.";

        return new AiSearchResult(productDTOs, message);
    }

}