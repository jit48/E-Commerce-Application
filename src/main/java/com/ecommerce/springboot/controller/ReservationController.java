package com.ecommerce.springboot.controller;

import com.ecommerce.springboot.dto.ReservationRequest;
import com.ecommerce.springboot.dto.ReservationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.springboot.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Slf4j
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        log.info("Request to create reservation for item ID: {}, customer: {}",
                request.getItemId(), request.getCustomerId());

        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable @NotNull Long reservationId,
            @RequestParam @NotBlank String customerId) {
        log.info("Request to cancel reservation ID: {} for customer: {}",
                reservationId, customerId);

        ReservationResponse response = reservationService.cancelReservation(reservationId, customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable @NotNull Long reservationId) {
        log.debug("Request to get reservation by ID: {}", reservationId);

        ReservationResponse response = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(response);
    }
}

