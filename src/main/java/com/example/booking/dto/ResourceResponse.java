package com.example.booking.dto;

import com.example.booking.entity.BookableResource;

import java.math.BigDecimal;

public record ResourceResponse(
        Long id,
        String name,
        String type,
        String description,
        boolean available,
        BigDecimal hourlyRate
) {
    public static ResourceResponse from(BookableResource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getType(),
                resource.getDescription(),
                resource.isAvailable(),
                resource.getHourlyRate()
        );
    }
}