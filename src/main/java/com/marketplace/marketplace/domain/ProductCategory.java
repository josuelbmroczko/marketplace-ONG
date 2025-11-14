package com.marketplace.marketplace.domain;

/**
 * Define as categorias de produtos padronizadas.
 */
public enum ProductCategory {

    ALIMENTO("Alimento"),
    BRINQUEDO("Brinquedo"),
    ACESSORIO("Acessório"),
    HIGIENE("Higiene"),
    MEDICAMENTO("Medicamento"),
    OUTRO("Outro");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}