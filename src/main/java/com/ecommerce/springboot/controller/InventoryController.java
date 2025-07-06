package com.ecommerce.springboot.controller;

import com.ecommerce.springboot.dto.CreateInventoryItemRequest;
import com.ecommerce.springboot.entity.InventoryItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.springboot.service.InventoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @Valid @RequestBody CreateInventoryItemRequest request) {
        log.info("Request to create inventory item: {}", request.getSku());

        InventoryItemResponse response = inventoryService.createInventoryItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/home")
    public ResponseEntity<String> home() {
        return ResponseEntity.status(HttpStatus.CREATED).body("Home");
    }

    @PostMapping("/{itemId}/supply")
    public ResponseEntity<InventoryItemResponse> addSupply(
            @PathVariable @NotNull Long itemId,
            @RequestParam @Positive Integer quantity) {
        log.info("Request to add supply to item ID: {}, quantity: {}", itemId, quantity);

        InventoryItemResponse response = inventoryService.addSupply(itemId, quantity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<InventoryItemResponse> getItemBySku(@PathVariable String sku) {
        log.debug("Request to get item by SKU: {}", sku);

        InventoryItemResponse response = inventoryService.getItemBySku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getAllActiveItems() {
        log.debug("Request to get all active items");

        List<InventoryItemResponse> response = inventoryService.getAllActiveItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{itemId}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable @NotNull Long itemId,
            @RequestParam @Positive Integer quantity) {
        log.debug("Availability for item ID: {}, quantity: {}", itemId, quantity);

        boolean available = inventoryService.isItemAvailable(itemId, quantity);
        return ResponseEntity.ok(available);
    }

    @PutMapping("/{itemId}/deactivate")
    public ResponseEntity<InventoryItemResponse> deactivateItem(@PathVariable @NotNull Long itemId) {
        log.info("Request to deactivate item ID: {}", itemId);

        InventoryItemResponse response = inventoryService.deactivateItem(itemId);
        return ResponseEntity.ok(response);
    }
}
