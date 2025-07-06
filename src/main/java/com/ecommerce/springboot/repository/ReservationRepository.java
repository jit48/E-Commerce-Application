package com.ecommerce.springboot.repository;

import com.ecommerce.springboot.entity.Reservation;
import com.ecommerce.springboot.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCustomerIdAndStatus(String customerId, ReservationStatus status);

    List<Reservation> findByItemIdAndStatus(Long itemId, ReservationStatus status);

    Optional<Reservation> findByIdAndCustomerId(Long id, String customerId);

    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("status") ReservationStatus status,
                                              @Param("now") LocalDateTime now);

    @Query("SELECT SUM(r.quantity) FROM Reservation r WHERE r.itemId = :itemId AND r.status = :status")
    Optional<Integer> getTotalReservedQuantity(@Param("itemId") Long itemId,
                                               @Param("status") ReservationStatus status);

}
