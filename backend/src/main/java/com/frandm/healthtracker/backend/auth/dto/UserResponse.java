package com.frandm.healthtracker.backend.auth.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name
) {
}
