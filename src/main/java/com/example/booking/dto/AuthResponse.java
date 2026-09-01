package com.example.booking.dto;

import com.example.booking.entity.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        Role role
) {
}