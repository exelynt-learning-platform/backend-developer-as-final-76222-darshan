package com.example.booking.dto;

import com.example.booking.entity.ReservationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservationRequest(
        @NotNull Long resourceId,
        @NotNull @Future OffsetDateTime startTime,
        @NotNull @Future OffsetDateTime endTime,
        ReservationStatus status,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal price
) {
}