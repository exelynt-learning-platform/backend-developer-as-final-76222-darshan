package com.example.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ResourceRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 60) String type,
        @Size(max = 1000) String description,
        boolean available,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal hourlyRate
) {
}