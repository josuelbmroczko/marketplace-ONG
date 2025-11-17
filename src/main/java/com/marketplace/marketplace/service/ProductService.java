package com.marketplace.marketplace.service;

import com.marketplace.marketplace.domain.*;
import com.marketplace.marketplace.dto.AiSearchResult;
import com.marketplace.marketplace.dto.ProductDTO;
import com.marketplace.marketplace.dto.SearchFilters;
import com.marketplace.marketplace.dto.AiSearchResponse;
import com.marketplace.marketplace.repository.OrganizationRepository;
import com.marketplace.marketplace.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;
    private final AiSearchService aiSearchService;

    public ProductService(ProductRepository productRepository,
                          OrganizationRepository organizationRepository,
                          AiSearchService aiSearchService) {
        this.productRepository = productRepository;
        this.organizationRepository = organizationRepository;
        this.aiSearchService = aiSearchService;
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
            if (productDetails.getOrganization() != null && productDetails.getOrganization().getId() != null) {
                Organization org = organizationRepository.findById(productDetails.getOrganization().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada"));
                product.setOrganization(org);
            } else {
                product.setOrganization(null);
            }
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
            if (product.getOrganization() == null || !product.getOrganization().getId().equals(loggedInUser.getOrganization().getId())) {
                throw new AccessDeniedException("Gerente não tem permissão para apagar este produto.");
            }
        }
        productRepository.delete(product);
    }


    public List<ProductDTO> findWithFilters(String name, BigDecimal minPrice, BigDecimal maxPrice, String category, String sort) {

        Specification<Product> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("organization", JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("productName")), "%" + name.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (category != null && !category.isEmpty()) {
                try {
                    ProductCategory catEnum = ProductCategory.valueOf(category.toUpperCase());
                    predicates.add(cb.equal(root.get("category"), catEnum));
                } catch (IllegalArgumentException e) {
                    System.err.println("Categoria inválida recebida: " + category);
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sortOrder = buildSortOrder(sort);

        List<Product> products = productRepository.findAll(spec, sortOrder);

        return products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    public AiSearchResult aiSearch(String aiQuery) {

        System.out.println("\n\n--- 1. ENTREI NA BUSCA DA IA (aiSearch) COM O TERMO: '" + aiQuery + "' ---");

        try {
            System.out.println("--- 2. CHAMANDO A IA (GEMINI)... ---");
            AiSearchResponse aiResponse = aiSearchService.parseSearchQuery(aiQuery);
            SearchFilters filters = aiResponse.getFilters();
            String friendlyMessage = aiResponse.getFriendlyMessage();
            System.out.println("--- 3. IA RESPONDEU COM FILTROS: " + filters.getName() + " | " + filters.getCategory() + " | " + filters.getSort() + " ---");

            Specification<Product> spec = (root, query, cb) -> {
                if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                    root.fetch("organization", JoinType.LEFT);
                }
                List<Predicate> predicates = new ArrayList<>();

                if (filters.getName() != null && !filters.getName().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("productName")), "%" + filters.getName().toLowerCase() + "%"));
                }
                if (filters.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filters.getMinPrice()));
                }
                if (filters.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), filters.getMaxPrice()));
                }
                if (filters.getCategory() != null && !filters.getCategory().isEmpty()) {
                    try {
                        ProductCategory catEnum = ProductCategory.valueOf(filters.getCategory().toUpperCase());
                        predicates.add(cb.equal(root.get("category"), catEnum));
                    } catch (IllegalArgumentException e) {
                        System.err.println("IA retornou categoria inválida: " + filters.getCategory());
                    }
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            };

            System.out.println("--- 4. EXECUTANDO BUSCA INTELIGENTE NO BANCO... ---");

            Sort sortOrder = buildSortOrder(filters.getSort());

            List<Product> products = productRepository.findAll(spec, sortOrder);
            System.out.println("--- 5. BUSCA INTELIGENTE ACHOU: " + products.size() + " PRODUTOS ---");

            if (products.isEmpty()) {
                System.out.println("--- 6. BUSCA INTELIGENTE FALHOU. ATIVANDO FALLBACK... ---");

                String fallbackQuery = (filters.getName() != null && !filters.getName().isEmpty()) ? filters.getName() : aiQuery;
                System.out.println("--- 6b. USANDO TERMO DE FALLBACK: '" + fallbackQuery + "' ---");

                String fallbackMessage = "Não achei com os filtros da IA... Mas veja se é um destes:";
                String message = (friendlyMessage == null || friendlyMessage.isEmpty()) ? fallbackMessage : friendlyMessage;

                return fallbackAiSearch(fallbackQuery, message, true);
            }

            List<ProductDTO> productDTOs = products.stream()
                    .map(ProductDTO::new)
                    .collect(Collectors.toList());

            System.out.println("--- 6. BUSCA INTELIGENTE TEVE SUCESSO. RETORNANDO " + productDTOs.size() + " PRODUTOS. ---");
            return new AiSearchResult(productDTOs, friendlyMessage, filters);

        } catch (Exception e) {
            System.out.println("--- 6. ERRO AO CHAMAR API DO GEMINI: " + e.getMessage() + " ---");
            return fallbackAiSearch(aiQuery, "Desculpe, a IA está offline. Fiz uma busca ampla por '" + aiQuery + "'.", false);
        }
    }

    private AiSearchResult fallbackAiSearch(String fallbackQuery, String message, boolean isAiFallback) {

        System.out.println("--- 7. ENTREI NO FALLBACK (BUSCA AMPLA) POR: '" + fallbackQuery + "' ---");

        Specification<Product> fuzzySpec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("organization", JoinType.LEFT);
            }

            String likePattern = "%" + fallbackQuery.toLowerCase() + "%";

            Predicate nameLike = cb.like(cb.lower(cb.coalesce(root.get("productName"), "")), likePattern);
            Predicate descriptionLike = cb.like(cb.lower(cb.coalesce(root.get("description"), "")), likePattern);
            Predicate categoryLike = cb.like(cb.lower(cb.coalesce(root.get("category").as(String.class), "")), likePattern);

            return cb.or(nameLike, descriptionLike, categoryLike);
        };

        List<Product> products = productRepository.findAll(fuzzySpec);
        System.out.println("--- 8. BUSCA AMPLA ACHOU: " + products.size() + " PRODUTOS ---");

        List<ProductDTO> productDTOs = products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());

        SearchFilters fallbackFilters = new SearchFilters(fallbackQuery, null, null, null);

        System.out.println("--- 9. SAINDO DO FALLBACK. --- \n\n");
        return new AiSearchResult(productDTOs, message, fallbackFilters);
    }

    private Sort buildSortOrder(String sort) {
        Sort sortOrder = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_asc":
                    sortOrder = Sort.by("price").ascending();
                    break;
                case "price_desc":
                    sortOrder = Sort.by("price").descending();
                    break;
                case "name_asc":
                    sortOrder = Sort.by("productName").ascending();
                    break;
            }
        }
        return sortOrder;
    }

    public List<ProductDTO> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, String category, String sort) {
        return findWithFilters(name, minPrice, maxPrice, category, sort);
    }
}