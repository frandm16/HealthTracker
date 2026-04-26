package com.frandm.healthtracker.backend.nutrition.food;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public final class FoodDtos {

    private FoodDtos() {
    }

    public record FoodRequest(
            @NotBlank String source,
            String sourceId,
            String barcode,
            @NotBlank String name,
            String brand,
            @NotNull @DecimalMin("0.0") BigDecimal caloriesPer100g,
            @NotNull @DecimalMin("0.0") BigDecimal proteinPer100g,
            @NotNull @DecimalMin("0.0") BigDecimal carbsPer100g,
            @NotNull @DecimalMin("0.0") BigDecimal fatPer100g
    ) {
    }

    public record FoodResponse(
            UUID id,
            String source,
            String sourceId,
            String barcode,
            String name,
            String brand,
            BigDecimal caloriesPer100g,
            BigDecimal proteinPer100g,
            BigDecimal carbsPer100g,
            BigDecimal fatPer100g
    ) {
    }
}
