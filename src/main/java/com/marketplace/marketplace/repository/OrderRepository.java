package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}