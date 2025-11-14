package com.marketplace.marketplace.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO que armazena os filtros estruturados retornados pela IA.
 */

public class SearchFilters {
    private String name;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public SearchFilters(String name, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        this.name = name;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}