package com.ecommerce.springboot.exception;

public class InsufficientStockException extends InventoryException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
