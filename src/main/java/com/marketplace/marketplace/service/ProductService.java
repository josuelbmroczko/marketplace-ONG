package com.marketplace.marketplace.service;

import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.dto.AiSearchResponse;
import com.marketplace.marketplace.dto.AiSearchResult;
import com.marketplace.marketplace.dto.ProductDTO;
import com.marketplace.marketplace.dto.SearchFilters;
import com.marketplace.marketplace.repository.ProductRepository;
import com.marketplace.marketplace.repository.ProductSpecification;
import net.logstash.logback.marker.Markers; // Importe os Markers
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductSpecification productSpecification;
    private final AiSearchService aiSearchService; // Interface da IA

    public ProductService(ProductRepository productRepository,
                          ProductSpecification productSpecification,
                          AiSearchService aiSearchService) { // Construtor atualizado
        this.productRepository = productRepository;
        this.productSpecification = productSpecification;
        this.aiSearchService = aiSearchService;
    }

    /**
     * Busca com filtros manuais (usada pela busca ao vivo / AJAX)
     */
    public List<ProductDTO> searchProducts(String name,
                                           BigDecimal minPrice,
                                           BigDecimal maxPrice,
                                           String category,
                                           String sortDirection) {

        Specification<Product> spec = productSpecification.findByFilters(name, minPrice, maxPrice, category);
        Sort sort = createSort(sortDirection);
        List<Product> products = productRepository.findAll(spec, sort);

        return products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca com IA (com Fallback e Logs Estruturados)
     */
    public AiSearchResult aiSearch(String query) {
        Specification<Product> spec;
        String friendlyMessage = null; // Mensagem padrão de fallback

        try {
            // 1. Tenta usar a IA
            AiSearchResponse aiResponse = aiSearchService.parseSearchQuery(query);
            SearchFilters filters = aiResponse.getFilters();

            // 2. LOG DE SUCESSO DA IA
            log.info("Busca por IA SUCESSO",
                    Markers.append("ai_query", query),
                    Markers.append("ai_filters_result", filters), // O DTO será serializado para JSON
                    Markers.append("ai_fallback", false)
            );

            spec = productSpecification.findByFilters(filters);
            friendlyMessage = aiResponse.getFriendlyMessage();

        } catch (Exception e) {
            // 3. LOG DE FALHA DA IA (FALLBACK)
            log.warn("Busca por IA FALHA (usando fallback)",
                    Markers.append("ai_query", query),
                    Markers.append("ai_error", e.getMessage()),
                    Markers.append("ai_fallback", true)
            );

            spec = productSpecification.findByAiFallback(query);
            friendlyMessage = "Desculpe, não entendi bem. Mas encontrei estes produtos com base no que você digitou!";
        }

        // O filtro de Tenant já está ativo em qualquer 'findAll'
        List<Product> products = productRepository.findAll(spec);

        List<ProductDTO> productDTOs = products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());

        // Retorna o DTO com a lista de produtos E a mensagem
        return new AiSearchResult(productDTOs, friendlyMessage);
    }

    /**
     * Helper privado para criar o objeto Sort
     */
    private Sort createSort(String sortDirection) {
        Sort sort = Sort.unsorted();
        if ("asc".equals(sortDirection)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("desc".equals(sortDirection)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        }
        return sort;
    }
}