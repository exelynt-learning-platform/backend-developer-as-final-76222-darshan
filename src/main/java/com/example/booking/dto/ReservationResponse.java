package com.example.booking.dto;

import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservationResponse(
        Long id,
        Long resourceId,
        String resourceName,
        Long userId,
        String username,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        ReservationStatus status,
        BigDecimal price
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getUser().getId(),
                reservation.getUser().getUsername(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getPrice()
        );
    }
}