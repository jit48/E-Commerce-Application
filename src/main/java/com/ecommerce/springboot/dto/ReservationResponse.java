package com.ecommerce.springboot.dto;


import com.ecommerce.springboot.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private Long itemId;
    private String customerId;
    private Integer quantity;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

}
