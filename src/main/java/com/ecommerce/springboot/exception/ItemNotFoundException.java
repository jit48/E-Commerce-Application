package com.ecommerce.springboot.exception;

public class ItemNotFoundException extends InventoryException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}

