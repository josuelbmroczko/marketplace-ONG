package com.marketplace.marketplace.dto;

import com.marketplace.marketplace.domain.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ShoppingCart {

    private List<com.marketplace.marketplace.dto.CartItem> items = new ArrayList<>();

    // Adiciona um item ou incrementa a quantidade se já existir
    public void addItem(Product product, int quantity) {
        for (com.marketplace.marketplace.dto.CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Se não encontrou, adiciona novo item
        items.add(new com.marketplace.marketplace.dto.CartItem(product, quantity));
    }

    // Remove um item pelo ID do produto
    public void removeItem(UUID productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    // Calcula o preço total do carrinho
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(com.marketplace.marketplace.dto.CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart() {
        items.clear();
    }

    // Getters e Setters
    public List<com.marketplace.marketplace.dto.CartItem> getItems() {
        return items;
    }

    public void setItems(List<com.marketplace.marketplace.dto.CartItem> items) {
        this.items = items;
    }
}