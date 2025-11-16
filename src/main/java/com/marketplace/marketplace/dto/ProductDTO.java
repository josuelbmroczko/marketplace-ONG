package com.marketplace.marketplace.dto;

import com.marketplace.marketplace.domain.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductDTO {

    private UUID id;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private String category;
    private String organizationName;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.quantity = product.getQuantity();
        this.imageUrl = product.getImageUrl();
        if (product.getCategory() != null) {
            this.category = product.getCategory().getDisplayName();
        }

        if (product.getOrganization() != null) {
            this.organizationName = product.getOrganization().getName();
        } else {
            this.organizationName = "Marketplace";
        }
    }
}