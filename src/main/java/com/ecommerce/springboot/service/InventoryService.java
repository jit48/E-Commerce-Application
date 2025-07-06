package com.ecommerce.springboot.service;

import com.ecommerce.springboot.dto.CreateInventoryItemRequest;
import com.ecommerce.springboot.entity.InventoryItem;
import com.ecommerce.springboot.entity.InventoryItemResponse;
import com.ecommerce.springboot.exception.InventoryException;
import com.ecommerce.springboot.exception.ItemNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.ecommerce.springboot.repository.InventoryRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    @CacheEvict(value = "inventory", allEntries = true)
    public InventoryItemResponse createInventoryItem(CreateInventoryItemRequest request) {
        log.info("Creating inventory item with SKU: {}", request.getSku());

        try {
            InventoryItem item = new InventoryItem(
                    request.getItemName(),
                    request.getSku(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getTotalQuantity()
            );

            InventoryItem savedItem = inventoryRepository.save(item);
            log.info("Successfully created inventory item with ID: {}", savedItem.getId());

            return mapToResponse(savedItem);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to create inventory item - duplicate SKU: {}", request.getSku());
            throw new InventoryException("Item with SKU '" + request.getSku() + "' already exists");
        }
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#itemId")
    public InventoryItemResponse deactivateItem(Long itemId) {
        log.info("Deactivating item ID: {}", itemId);

        InventoryItem item = inventoryRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with ID " + itemId + " not found"));

        item.setActive(false);
        InventoryItem savedItem = inventoryRepository.save(item);
        log.info("Successfully deactivated item ID: {}", itemId);

        return mapToResponse(savedItem);
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#itemId")
    public InventoryItemResponse addSupply(Long itemId, Integer quantity) {
        log.info("Adding supply to item ID: {}, quantity: {}", itemId, quantity);

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        InventoryItem item = inventoryRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with ID " + itemId + " not found"));

        if (!item.getActive()) {
            throw new InventoryException("Cannot add supply to inactive item with ID " + itemId);
        }

        Integer currentTotal = item.getTotalQuantity();

        item.setTotalQuantity(currentTotal + quantity);
        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);

        InventoryItem savedItem = inventoryRepository.save(item);
        log.info("Successfully added {} units to item ID: {}. New total: {}",
                quantity, itemId, currentTotal + quantity);

        return mapToResponse(savedItem);
    }

    @Transactional
    @Cacheable(value = "inventory", key = "'sku_' + #sku")
    public InventoryItemResponse getItemBySku(String sku) {
        log.info("Retrieving item with SKU: {}", sku);

        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }

        InventoryItem item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new ItemNotFoundException("Item with SKU '" + sku + "' not found"));

        log.debug("Successfully retrieved item with SKU: {}", sku);
        return mapToResponse(item);
    }

    @Transactional
    @Cacheable(value = "inventory", key = "'all_active_items'")
    public List<InventoryItemResponse> getAllActiveItems() {
        log.info("Retrieving all active inventory items");

        List<InventoryItem> activeItems = inventoryRepository.findByActiveTrue();

        List<InventoryItemResponse> responses = activeItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Successfully retrieved {} active inventory items", responses.size());
        return responses;
    }

    @Transactional
    public boolean isItemAvailable(Long itemId, Integer quantity) {
        log.info("Checking availability for item ID: {}, requested quantity: {}", itemId, quantity);

        if (quantity == null || quantity <= 0) {
            log.warn("Invalid quantity requested: {}", quantity);
            return false;
        }

        InventoryItem item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with ID " + itemId + " not found"));

        if (!item.getActive()) {
            log.warn("Item with ID {} is not active", itemId);
            return false;
        }

        boolean isAvailable = item.getAvailableQuantity() >= quantity;
        log.info("Item ID: {} availability check - requested: {}, available: {}, result: {}",
                itemId, quantity, item.getAvailableQuantity(), isAvailable);

        return isAvailable;
    }
    private InventoryItemResponse mapToResponse(InventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getItemName(),
                item.getSku(),
                item.getDescription(),
                item.getPrice(),
                item.getTotalQuantity(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getActive(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}