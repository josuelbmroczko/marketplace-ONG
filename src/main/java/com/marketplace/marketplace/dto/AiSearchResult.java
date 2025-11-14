package com.marketplace.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class AiSearchResult {

    private List<ProductDTO> products;
    private String friendlyMessage;

    public AiSearchResult(List<ProductDTO> products, String friendlyMessage) {
        this.products = products;
        this.friendlyMessage = friendlyMessage;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }

    public String getFriendlyMessage() {
        return friendlyMessage;
    }

    public void setFriendlyMessage(String friendlyMessage) {
        this.friendlyMessage = friendlyMessage;
    }


}