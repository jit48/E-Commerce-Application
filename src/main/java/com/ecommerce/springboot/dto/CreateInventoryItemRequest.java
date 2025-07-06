package com.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CreateInventoryItemRequest {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String description;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Total quantity is required")
    @PositiveOrZero(message = "Total quantity must be positive")
    private Integer totalQuantity;
}
