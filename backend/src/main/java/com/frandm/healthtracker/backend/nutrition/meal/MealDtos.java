package com.frandm.healthtracker.backend.nutrition.meal;

import com.frandm.healthtracker.backend.nutrition.meal.MealSlotEntity.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class MealDtos {

    private MealDtos() {
    }

    public record MealSlotResponse(
            UUID id,
            MealType mealType,
            List<MealItemResponse> items,
            List<MealDishResponse> dishes
    ) {
    }

    public record MealItemRequest(
            UUID foodId,
            @NotBlank String foodName,
            String brand,
            @NotNull @Positive BigDecimal quantityG,
            @NotNull @DecimalMin("0.0") BigDecimal caloriesKcal,
            @NotNull @DecimalMin("0.0") BigDecimal proteinG,
            @NotNull @DecimalMin("0.0") BigDecimal carbsG,
            @NotNull @DecimalMin("0.0") BigDecimal fatsG
    ) {
    }

    public record MealItemResponse(
            UUID id,
            UUID foodId,
            String foodName,
            String brand,
            BigDecimal quantityG,
            BigDecimal caloriesKcal,
            BigDecimal proteinG,
            BigDecimal carbsG,
            BigDecimal fatsG
    ) {
    }

    public record MealDishRequest(
            @NotBlank String name,
            String description,
            @NotNull List<UUID> mealItemIds
    ) {
    }

    public record MealDishResponse(
            UUID id,
            String name,
            String description,
            BigDecimal caloriesKcal,
            BigDecimal proteinG,
            BigDecimal carbsG,
            BigDecimal fatsG,
            List<UUID> mealItemIds
    ) {
    }
}
