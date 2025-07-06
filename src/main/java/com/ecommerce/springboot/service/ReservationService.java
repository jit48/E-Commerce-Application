package com.ecommerce.springboot.service;

import com.ecommerce.springboot.dto.ReservationRequest;
import com.ecommerce.springboot.dto.ReservationResponse;
import com.ecommerce.springboot.entity.InventoryItem;
import com.ecommerce.springboot.entity.Reservation;
import com.ecommerce.springboot.entity.ReservationStatus;
import com.ecommerce.springboot.exception.InsufficientStockException;
import com.ecommerce.springboot.exception.ItemNotFoundException;
import com.ecommerce.springboot.exception.ReservationNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.ecommerce.springboot.repository.InventoryRepository;
import com.ecommerce.springboot.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    @CacheEvict(value = "inventory", key = "#request.itemId")
    public ReservationResponse createReservation(ReservationRequest request) {
        log.info("Creating reservation for item ID: {}, customer: {}, quantity: {}",
                request.getItemId(), request.getCustomerId(), request.getQuantity());


        InventoryItem item = inventoryRepository.findByIdWithLock(request.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item with ID " + request.getItemId() + " not found"));


        if (!item.getActive()) {
            throw new ItemNotFoundException("Item with ID " + request.getItemId() + " is not active");
        }


        if (item.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            item.getAvailableQuantity(), request.getQuantity())
            );
        }


        item.setAvailableQuantity(item.getAvailableQuantity() - request.getQuantity());
        item.setReservedQuantity(item.getReservedQuantity() + request.getQuantity());
        inventoryRepository.save(item);


        Reservation reservation = new Reservation(
                request.getItemId(),
                request.getCustomerId(),
                request.getQuantity()
        );

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Successfully created reservation ID: {}", savedReservation.getId());

        return mapToResponse(savedReservation);
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#reservation.itemId")
    public ReservationResponse cancelReservation(Long reservationId, String customerId) {
        log.info("Cancelling reservation ID: {} for customer: {}", reservationId, customerId);

        Reservation reservation = reservationRepository.findByIdAndCustomerId(reservationId, customerId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation with ID " + reservationId + " not found for customer " + customerId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ReservationNotFoundException(
                    "Reservation with ID " + reservationId + " is not active");
        }


        InventoryItem item = inventoryRepository.findByIdWithLock(reservation.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item with ID " + reservation.getItemId() + " not found"));


        item.setAvailableQuantity(item.getAvailableQuantity() + reservation.getQuantity());
        item.setReservedQuantity(item.getReservedQuantity() - reservation.getQuantity());
        inventoryRepository.save(item);


        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation savedReservation = reservationRepository.save(reservation);

        log.info("Successfully cancelled reservation ID: {}", reservationId);
        return mapToResponse(savedReservation);
    }

    @Transactional
    public ReservationResponse getReservationById(Long reservationId) {
        log.debug("Fetching reservation by ID: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationId + " not found"));

        return mapToResponse(reservation);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getItemId(),
                reservation.getCustomerId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getExpiresAt()
        );
    }
}

