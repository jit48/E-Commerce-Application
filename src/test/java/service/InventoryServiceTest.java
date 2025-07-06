package service;

import com.ecommerce.springboot.dto.CreateInventoryItemRequest;
import com.ecommerce.springboot.entity.InventoryItem;
import com.ecommerce.springboot.entity.InventoryItemResponse;
import com.ecommerce.springboot.exception.InventoryException;
import com.ecommerce.springboot.exception.ItemNotFoundException;
import com.ecommerce.springboot.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import com.ecommerce.springboot.repository.InventoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private InventoryItem sampleItem() {
        InventoryItem item = new InventoryItem("Sample Item", "SKU123", "Sample Desc",
                BigDecimal.valueOf(99.99), 10);
        item.setId(1L);
        item.setAvailableQuantity(10);
        item.setReservedQuantity(0);
        item.setActive(true);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    @Test
    void testCreateInventoryItem_Success() {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                "Item A", "SKU001", "Desc A", BigDecimal.valueOf(100), 50
        );

        InventoryItem savedItem = sampleItem();
        when(inventoryRepository.save(any())).thenReturn(savedItem);

        InventoryItemResponse response = inventoryService.createInventoryItem(request);

        assertEquals("SKU123", response.getSku());
        verify(inventoryRepository, times(1)).save(any());
    }

    @Test
    void testCreateInventoryItem_DuplicateSku_ThrowsException() {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                "Item A", "SKU001", "Desc A", BigDecimal.valueOf(100), 50
        );

        when(inventoryRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThrows(InventoryException.class, () -> inventoryService.createInventoryItem(request));
    }

    @Test
    void testGetItemBySku_Success() {
        InventoryItem item = sampleItem();
        when(inventoryRepository.findBySku("SKU123")).thenReturn(Optional.of(item));

        InventoryItemResponse response = inventoryService.getItemBySku("SKU123");

        assertEquals("SKU123", response.getSku());
    }

    @Test
    void testGetItemBySku_NotFound() {
        when(inventoryRepository.findBySku("SKU999")).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> inventoryService.getItemBySku("SKU999"));
    }

    @Test
    void testAddSupply_Success() {
        InventoryItem item = sampleItem();
        when(inventoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenReturn(item);

        InventoryItemResponse response = inventoryService.addSupply(1L, 5);

        assertEquals(15, response.getTotalQuantity());
    }

    @Test
    void testAddSupply_ToInactiveItem_ThrowsException() {
        InventoryItem item = sampleItem();
        item.setActive(false);

        when(inventoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(item));

        assertThrows(InventoryException.class, () -> inventoryService.addSupply(1L, 5));
    }

    @Test
    void testDeactivateItem_Success() {
        InventoryItem item = sampleItem();
        when(inventoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenReturn(item);

        InventoryItemResponse response = inventoryService.deactivateItem(1L);
        assertFalse(response.getActive());
    }

    @Test
    void testIsItemAvailable_True() {
        InventoryItem item = sampleItem();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        assertTrue(inventoryService.isItemAvailable(1L, 5));
    }

    @Test
    void testIsItemAvailable_InsufficientQuantity() {
        InventoryItem item = sampleItem();
        item.setAvailableQuantity(2);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        assertFalse(inventoryService.isItemAvailable(1L, 5));
    }

    @Test
    void testGetAllActiveItems() {
        List<InventoryItem> list = List.of(sampleItem());
        when(inventoryRepository.findByActiveTrue()).thenReturn(list);

        List<InventoryItemResponse> response = inventoryService.getAllActiveItems();
        assertEquals(1, response.size());
    }
}

