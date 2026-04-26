package com.frandm.healthtracker.backend.nutrition.profile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public final class NutritionProfileDtos {

    private NutritionProfileDtos() {
    }

    public record NutritionProfileRequest(
            @NotNull @DecimalMin("0.0") @Max(100) BigDecimal proteinPercentage,
            @NotNull @DecimalMin("0.0") @Max(100) BigDecimal carbsPercentage,
            @NotNull @DecimalMin("0.0") @Max(100) BigDecimal fatsPercentage
    ) {
    }

    public record NutritionProfileResponse(
            UUID id,
            BigDecimal proteinPercentage,
            BigDecimal carbsPercentage,
            BigDecimal fatsPercentage
    ) {
    }
}
