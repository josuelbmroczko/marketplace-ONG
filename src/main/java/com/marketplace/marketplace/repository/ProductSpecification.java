package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.ProductCategory;
import com.marketplace.marketplace.dto.SearchFilters;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSpecification {

    public Specification<Product> findByFilters(SearchFilters filters) {
        return (root, query, cb) -> {
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
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Product> findByFilters(String name, BigDecimal minPrice, BigDecimal maxPrice, String category) {
        return (root, query, cb) -> {
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
                    ProductCategory catEnum = ProductCategory.valueOf(category);
                    predicates.add(cb.equal(root.get("category"), catEnum));
                } catch (IllegalArgumentException e) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Product> findByAiFallback(String queryString) {
        return (root, query, cb) -> {
            String likeQuery = "%" + queryString.toLowerCase() + "%";

            Predicate nameMatch = cb.like(cb.lower(root.get("productName")), likeQuery);
            Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), likeQuery);

            return cb.or(nameMatch, descriptionMatch);
        };
    }
}