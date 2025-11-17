package com.marketplace.marketplace.dto;


import lombok.Data;
import java.util.List;
@Data
public class AiSearchResult {

    private List<ProductDTO> products;
    private String friendlyMessage;
    private SearchFilters filters;

    public AiSearchResult(List<ProductDTO> products, String friendlyMessage, SearchFilters filters) {
        this.products = products;
        this.friendlyMessage = friendlyMessage;
        this.filters = filters;
    }

    public String getFriendlyMessage() {
        return friendlyMessage;
    }

    public void setFriendlyMessage(String friendlyMessage) {
        this.friendlyMessage = friendlyMessage;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }

    public SearchFilters getFilters() {
        return filters;
    }

    public void setFilters(SearchFilters filters) {
        this.filters = filters;
    }
}