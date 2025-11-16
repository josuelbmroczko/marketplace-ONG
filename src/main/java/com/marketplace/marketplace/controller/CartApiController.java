package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.ShoppingCart;
import com.marketplace.marketplace.repository.OrderRepository;
import com.marketplace.marketplace.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public CartApiController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    private ShoppingCart getCart(HttpSession session) {
        ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
        if (cart == null) {
            cart = new ShoppingCart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @GetMapping
    public ShoppingCart showCart(HttpSession session) {
        return getCart(session);
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<ShoppingCart> addToCart(@PathVariable("productId") UUID productId,
                                                  @RequestBody Map<String, Integer> payload,
                                                  HttpSession session) {

        int quantity = payload.getOrDefault("quantity", 1);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        if (product.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque insuficiente");
        }

        ShoppingCart cart = getCart(session);
        cart.addItem(product, quantity);
        session.setAttribute("cart", cart);

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ShoppingCart removeFromCart(@PathVariable("productId") UUID productId, HttpSession session) {
        ShoppingCart cart = getCart(session);
        cart.removeItem(productId);
        session.setAttribute("cart", cart);
        return cart;
    }

}