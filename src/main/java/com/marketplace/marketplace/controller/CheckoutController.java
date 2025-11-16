package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Order;
import com.marketplace.marketplace.domain.OrderItem;
import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.User;
import com.marketplace.marketplace.dto.CartItem;
import com.marketplace.marketplace.dto.ShoppingCart;
import com.marketplace.marketplace.repository.OrderRepository;
import com.marketplace.marketplace.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public CheckoutController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    private ShoppingCart getCart(HttpSession session) {
        ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
        if (cart == null) {
            cart = new ShoppingCart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @PostMapping("/process")
    @Transactional
    public ResponseEntity<?> processCheckout(
            HttpSession session,
            @AuthenticationPrincipal User user,
            @RequestParam String nome,
            @RequestParam String endereco) {

        ShoppingCart cart = getCart(session);
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio");
        }

        List<Product> productsToUpdate = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

            if (product.getQuantity() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque insuficiente para " + product.getProductName());
            }
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productsToUpdate.add(product);
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setOrganization(user.getOrganization());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        orderRepository.save(order);
        productRepository.saveAll(productsToUpdate);

        cart.clearCart();
        session.setAttribute("cart", cart);

        return ResponseEntity.ok(Map.of("message", "Pedido realizado com sucesso!"));
    }
}