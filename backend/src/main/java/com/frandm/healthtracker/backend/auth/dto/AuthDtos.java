package com.frandm.healthtracker.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserResponse user
    ) {
    }

    public record GoogleSignInRequest(
            @NotBlank String idToken
    ) {
    }

    public record RefreshTokenRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record UserResponse(
            UUID id,
            String email,
            String name
    ) {
    }
}
