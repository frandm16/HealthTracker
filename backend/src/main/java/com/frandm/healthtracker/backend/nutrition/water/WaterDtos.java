package com.frandm.healthtracker.backend.nutrition.water;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class WaterDtos {

    private WaterDtos() {
    }

    public record WaterLogRequest(
            @NotNull @Min(1) Integer amountMl,
            OffsetDateTime recordedAt
    ) {
    }

    public record WaterLogResponse(
            UUID id,
            Integer amountMl,
            OffsetDateTime recordedAt
    ) {
    }
}
