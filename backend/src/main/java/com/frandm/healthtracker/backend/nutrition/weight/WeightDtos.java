package com.frandm.healthtracker.backend.nutrition.weight;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class WeightDtos {

    private WeightDtos() {
    }

    public record WeightLogRequest(
            @NotNull @DecimalMin("0.1") BigDecimal weightKg,
            OffsetDateTime recordedAt
    ) {
    }

    public record WeightLogResponse(
            UUID id,
            BigDecimal weightKg,
            OffsetDateTime recordedAt
    ) {
    }
}
