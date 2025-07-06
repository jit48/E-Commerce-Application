package com.ecommerce.springboot.exception;

public class ReservationNotFoundException extends InventoryException {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}
