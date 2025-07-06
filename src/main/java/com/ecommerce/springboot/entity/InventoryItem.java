package com.ecommerce.springboot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "inventory_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    @Column(nullable = false, unique = true)
    private String itemName;

    @NotBlank(message = "SKU is required")
    @Column(nullable = false, unique = true)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive")
    @Column(nullable = false)
    private BigDecimal price;

    @NotNull(message = "Total quantity is required")
    @PositiveOrZero(message = "Total quantity must be positive")
    @Column(nullable = false)
    private Integer totalQuantity;

    @NotNull(message = "Available quantity is required")
    @PositiveOrZero(message = "Available quantity must be positive")
    @Column(nullable = false)
    private Integer availableQuantity;

    @NotNull(message = "Reserved quantity is required")
    @PositiveOrZero(message = "Reserved quantity must be positive")
    @Column(nullable = false)
    private Integer reservedQuantity;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public InventoryItem(String itemName, String sku, String description, BigDecimal price, Integer totalQuantity) {
        this.itemName = itemName;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.reservedQuantity = 0;
        this.active = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return Objects.equals(id, that.id) && Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku);
    }
}

