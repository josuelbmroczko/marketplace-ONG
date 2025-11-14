package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.domain.Order; // Importe Order
import com.marketplace.marketplace.domain.Product;
import com.marketplace.marketplace.domain.User; // Importe User
import com.marketplace.marketplace.dto.CartItem;
import com.marketplace.marketplace.dto.ShoppingCart;
import com.marketplace.marketplace.repository.OrderRepository; // Importe OrderRepository
import com.marketplace.marketplace.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication; // Importe
import org.springframework.security.core.context.SecurityContextHolder; // Importe
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime; // Importe
import java.util.UUID;

@Controller
public class CartController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository; // 1. INJETE O REPO DE PEDIDOS

    public CartController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository; // 2. ATUALIZE O CONSTRUTOR
    }

    private ShoppingCart getCart(HttpSession session) {
        ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
        if (cart == null) {
            cart = new ShoppingCart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable("productId") UUID productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {

        // findById() já é filtrado pelo TenantInterceptor.
        // Um usuário da ONG A não pode adicionar ao carrinho um produto da ONG B.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        if (product.getQuantity() < quantity) {
            return "redirect:/produto?error=stock";
        }

        ShoppingCart cart = getCart(session);
        cart.addItem(product, quantity);
        session.setAttribute("cart", cart);

        return "redirect:/produto";
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        ShoppingCart cart = getCart(session);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") UUID productId, HttpSession session) {
        ShoppingCart cart = getCart(session);
        cart.removeItem(productId);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        ShoppingCart cart = getCart(session);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        return "checkout";
    }

    @PostMapping("/checkout/process")
    public String processCheckout(HttpSession session, @RequestParam("nome") String nome, @RequestParam("endereco") String endereco) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal(); // Pega o usuário logado

        ShoppingCart cart = getCart(session);

        // 3. ATUALIZAÇÃO DA LÓGICA DE "COMPRA"
        // (Validação de estoque)
        for (CartItem item : cart.getItems()) {
            Product dbProduct = productRepository.findById(item.getProduct().getId()).orElseThrow();
            if (dbProduct.getQuantity() < item.getQuantity()) {
                return "redirect:/cart?error=stock_final";
            }
        }

        // (Abater o estoque)
        for (CartItem item : cart.getItems()) {
            Product dbProduct = productRepository.findById(item.getProduct().getId()).orElseThrow();
            dbProduct.setQuantity(dbProduct.getQuantity() - item.getQuantity());
            productRepository.save(dbProduct);
        }

        // (Salvar o pedido)
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        // 4. A PARTE IMPORTANTE DO MULTI-TENANCY
        // Atribui o pedido à MESMA ONG do usuário que comprou
        order.setOrganization(user.getOrganization());

        // (Aqui você também criaria os OrderItems e os salvaria)

        orderRepository.save(order); // Salva o pedido

        session.removeAttribute("cart");

        return "redirect:/order-success";
    }

    @GetMapping("/order-success")
    public String showOrderSuccess() {
        return "order-success";
    }
}