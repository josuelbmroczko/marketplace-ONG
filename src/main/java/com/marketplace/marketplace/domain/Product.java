package com.marketplace.marketplace.domain;

import jakarta.persistence.*;
// Imports do Filtro
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.util.UUID;

// 1. DEFINIÇÃO GLOBAL DO FILTRO
// Define um filtro chamado "tenantFilter" que aceita um parâmetro "organizationId"
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "organizationId", type = UUID.class))

// 2. APLICAÇÃO DO FILTRO
// Aplica o filtro em todas as buscas (SELECTs) para esta entidade
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")

@Entity
@Table(name = "produto")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(name = "productName")
    private String productName;

    @Column(name = "amout")
    private String amout;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    // Construtor vazio (Necessário para JPA)
    public Product() {
    }

    // --- Getters e Setters ---
    // (Omitidos por brevidade, mas devem estar aqui,
    // ou você pode usar @Data do Lombok no topo da classe)

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getAmout() { return amout; }
    public void setAmout(String amout) { this.amout = amout; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}