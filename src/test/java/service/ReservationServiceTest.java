package service;

import com.ecommerce.springboot.dto.ReservationRequest;
import com.ecommerce.springboot.dto.ReservationResponse;
import com.ecommerce.springboot.entity.InventoryItem;
import com.ecommerce.springboot.entity.Reservation;
import com.ecommerce.springboot.entity.ReservationStatus;
import com.ecommerce.springboot.exception.InsufficientStockException;
import com.ecommerce.springboot.exception.ItemNotFoundException;
import com.ecommerce.springboot.exception.ReservationNotFoundException;
import com.ecommerce.springboot.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import com.ecommerce.springboot.repository.InventoryRepository;
import com.ecommerce.springboot.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private InventoryItem sampleItem() {
        InventoryItem item = new InventoryItem("Sample Item", "SKU123", "Desc",
                new java.math.BigDecimal("100.00"), 10);
        item.setId(1L);
        item.setActive(true);
        item.setAvailableQuantity(5);
        item.setReservedQuantity(0);
        return item;
    }

    private Reservation sampleReservation() {
        Reservation reservation = new Reservation(1L, "cust123", 2);
        reservation.setId(100L);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        return reservation;
    }

    @Test
    void testCreateReservation_Success() {
        ReservationRequest request = new ReservationRequest(1L, "cust123", 2);
        InventoryItem item = sampleItem();

        when(inventoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenReturn(item);
        when(reservationRepository.save(any())).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(101L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(1L, response.getItemId());
        assertEquals("cust123", response.getCustomerId());
        assertEquals(2, response.getQuantity());
    }

    @Test
    void testCreateReservation_ItemNotFound() {
        ReservationRequest request = new ReservationRequest(1L, "cust123", 2);

        when(inventoryRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void testCreateReservation_InsufficientStock() {
        ReservationRequest request = new ReservationRequest(1L, "cust123", 10);
        InventoryItem item = sampleItem();
        item.setAvailableQuantity(5);

        when(inventoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(item));

        assertThrows(InsufficientStockException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void testCancelReservation_Success() {
        Reservation reservation = sampleReservation();
        InventoryItem item = sampleItem();

        when(reservationRepository.findByIdAndCustomerId(100L, "cust123")).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(item));
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(inventoryRepository.save(any())).thenReturn(item);

        ReservationResponse response = reservationService.cancelReservation(100L, "cust123");

        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
    }

    @Test
    void testCancelReservation_NotFound() {
        when(reservationRepository.findByIdAndCustomerId(100L, "cust123")).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class,
                () -> reservationService.cancelReservation(100L, "cust123"));
    }

    @Test
    void testCancelReservation_ItemNotFound() {
        Reservation reservation = sampleReservation();

        when(reservationRepository.findByIdAndCustomerId(100L, "cust123")).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findByIdWithLock(any())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> reservationService.cancelReservation(100L, "cust123"));
    }

    @Test
    void testGetReservationById_Success() {
        Reservation reservation = sampleReservation();

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(100L);

        assertEquals("cust123", response.getCustomerId());
    }

    @Test
    void testGetReservationById_NotFound() {
        when(reservationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class,
                () -> reservationService.getReservationById(100L));
    }
}

