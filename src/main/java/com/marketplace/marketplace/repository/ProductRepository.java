package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.domain.Organization;
import com.marketplace.marketplace.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> {

    List<Product> findByOrganization(Organization organization);
}