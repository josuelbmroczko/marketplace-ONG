package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}